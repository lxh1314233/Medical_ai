package com.xianhuicode.medical_ai.triage.service.impl;

import com.xianhuicode.medical_ai.ai.guard.GuardResult;
import com.xianhuicode.medical_ai.ai.guard.RedFlagGuard;
import com.xianhuicode.medical_ai.ai.llm.LlmGateway;
import com.xianhuicode.medical_ai.common.api.ResultCode;
import com.xianhuicode.medical_ai.common.exception.BizException;
import com.xianhuicode.medical_ai.triage.dto.TriageReq;
import com.xianhuicode.medical_ai.triage.dto.TriageVO;
import com.xianhuicode.medical_ai.triage.entity.TriageRecord;
import com.xianhuicode.medical_ai.triage.mapper.TriageRecordMapper;
import com.xianhuicode.medical_ai.triage.service.TriageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

/**
 * 症状分诊实现：红旗拦截（最高优先级）→ 结构化分诊（LLM 输出 JSON）→ 落库。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TriageServiceImpl implements TriageService {

    private static final String DISCLAIMER = "本结果仅供挂号参考，不构成诊断依据，最终请以医生面诊为准。";

    private static final String SYSTEM_PROMPT = """
            你是一名导诊助手，根据用户描述的症状推荐 1~3 个最合适的就诊科室，并给出紧急程度分级。
            严格只输出一个 JSON 对象，不要输出任何其他文字，不要用 markdown 代码块，字段如下：
            {"departments":["科室1","科室2"],"reasons":["推荐理由1","理由2"],"urgencyLevel":"立即就医|尽快就医|先观察","observationNotes":"观察要点"}
            其中 departments 和 reasons 必须是字符串数组，urgencyLevel 只能取「立即就医」「尽快就医」「先观察」三者之一。
            """;

    private final RedFlagGuard redFlagGuard;
    private final LlmGateway llmGateway;
    private final TriageRecordMapper triageRecordMapper;
    private final ObjectMapper objectMapper;

    @Override
    public TriageVO triage(Long userId, TriageReq req) {
        String symptomText = buildSymptomText(req);
        if (symptomText.isBlank()) {
            throw new BizException(ResultCode.BIZ_PARAM_ERROR);
        }

        // 1. 红旗症状拦截（最高优先级）：命中立即引导 120/急诊，终止普通分诊
        Optional<GuardResult> redFlag = redFlagGuard.check(symptomText);
        if (redFlag.isPresent()) {
            TriageVO vo = new TriageVO();
            vo.setRedFlag(true);
            vo.setRedFlagMessage(redFlag.get().reply());
            save(userId, symptomText, null, null, 1, redFlag.get().reply());
            return vo;
        }

        // 2. 结构化分诊：LLM 输出 JSON → 解析
        TriageVO vo = callLlm(symptomText);

        // 3. 落库
        save(userId, symptomText, vo.getDepartments(), vo.getUrgencyLevel(), 0, buildSummary(vo));
        return vo;
    }

    private TriageVO callLlm(String symptomText) {
        try {
            List<LlmGateway.LlmMessage> messages = List.of(
                    new LlmGateway.LlmMessage("system", SYSTEM_PROMPT),
                    new LlmGateway.LlmMessage("user", symptomText));
            String raw = llmGateway.generate(messages);
            String json = extractJson(raw);
            if (json == null) {
                throw new IllegalStateException("模型未返回有效 JSON");
            }
            TriageVO vo = objectMapper.readValue(json, TriageVO.class);
            vo.setRedFlag(false);
            vo.setDisclaimer(DISCLAIMER);
            return vo;
        } catch (Exception e) {
            log.warn("结构化分诊失败，返回兜底结果: {}", e.getMessage());
            return fallback();
        }
    }

    private TriageVO fallback() {
        TriageVO vo = new TriageVO();
        vo.setRedFlag(false);
        vo.setDepartments(List.of("全科"));
        vo.setUrgencyLevel("尽快就医");
        vo.setReasons(List.of("未能自动完成分诊判断，建议线下咨询"));
        vo.setObservationNotes("建议前往就近医院或社区全科门诊面诊确认。");
        vo.setDisclaimer(DISCLAIMER);
        return vo;
    }

    private String buildSymptomText(TriageReq req) {
        StringBuilder sb = new StringBuilder();
        append(sb, "主诉", req.getChiefComplaint());
        append(sb, "持续时间", req.getDuration());
        append(sb, "严重程度", req.getSeverity());
        append(sb, "伴随症状", req.getAccompanyingSymptoms());
        append(sb, "既往史", req.getPastHistory());
        return sb.toString();
    }

    private void append(StringBuilder sb, String label, String value) {
        if (value != null && !value.isBlank()) {
            if (!sb.isEmpty()) {
                sb.append("\n");
            }
            sb.append(label).append("：").append(value.trim());
        }
    }

    private String buildSummary(TriageVO vo) {
        String dept = vo.getDepartments() == null ? "" : String.join("、", vo.getDepartments());
        String obs = vo.getObservationNotes() == null ? "" : "；观察要点：" + vo.getObservationNotes();
        return "推荐科室：" + dept + "；紧急程度：" + vo.getUrgencyLevel() + obs;
    }

    private void save(Long userId, String symptomText, List<String> departments,
                      String urgencyLevel, int redFlag, String summary) {
        TriageRecord record = new TriageRecord();
        record.setUserId(userId);
        record.setSymptomInput(symptomText);
        try {
            record.setDepartments(departments == null ? null : objectMapper.writeValueAsString(departments));
        } catch (Exception e) {
            record.setDepartments(null);
        }
        record.setUrgencyLevel(urgencyLevel);
        record.setRedFlag(redFlag);
        record.setResultSummary(summary);
        triageRecordMapper.insert(record);
    }

    private String extractJson(String s) {
        if (s == null) {
            return null;
        }
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return null;
        }
        return s.substring(start, end + 1);
    }
}
