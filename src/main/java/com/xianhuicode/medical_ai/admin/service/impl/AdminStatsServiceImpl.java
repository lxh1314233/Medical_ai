package com.xianhuicode.medical_ai.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xianhuicode.medical_ai.admin.dto.StatsOverviewVO;
import com.xianhuicode.medical_ai.admin.dto.TopQuestionVO;
import com.xianhuicode.medical_ai.admin.service.AdminStatsService;
import com.xianhuicode.medical_ai.auth.mapper.SysUserMapper;
import com.xianhuicode.medical_ai.chat.entity.ChatMessage;
import com.xianhuicode.medical_ai.chat.mapper.ChatMessageMapper;
import com.xianhuicode.medical_ai.feedback.entity.Feedback;
import com.xianhuicode.medical_ai.feedback.mapper.FeedbackMapper;
import com.xianhuicode.medical_ai.report.mapper.ReportRecordMapper;
import com.xianhuicode.medical_ai.triage.mapper.TriageRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据统计（ADMIN-03）：各类计数看板 + 高频问题（内存聚合，demo 规模够用）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminStatsServiceImpl implements AdminStatsService {

    private final SysUserMapper sysUserMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final TriageRecordMapper triageRecordMapper;
    private final ReportRecordMapper reportRecordMapper;
    private final FeedbackMapper feedbackMapper;

    @Override
    public StatsOverviewVO overview() {
        StatsOverviewVO vo = new StatsOverviewVO();
        vo.setUserCount(sysUserMapper.selectCount(null));
        vo.setChatQuestionCount(chatMessageMapper.selectCount(
                Wrappers.<ChatMessage>lambdaQuery().eq(ChatMessage::getRole, "user")));
        vo.setTriageCount(triageRecordMapper.selectCount(null));
        vo.setReportCount(reportRecordMapper.selectCount(null));
        vo.setFeedbackCount(feedbackMapper.selectCount(null));
        vo.setPendingCorrectionCount(feedbackMapper.selectCount(
                Wrappers.<Feedback>lambdaQuery()
                        .eq(Feedback::getType, "CORRECTION")
                        .eq(Feedback::getStatus, "PENDING")));

        long helpful = feedbackMapper.selectCount(
                Wrappers.<Feedback>lambdaQuery().eq(Feedback::getType, "HELPFUL"));
        long notHelpful = feedbackMapper.selectCount(
                Wrappers.<Feedback>lambdaQuery().eq(Feedback::getType, "NOT_HELPFUL"));
        long total = helpful + notHelpful;
        vo.setHelpfulCount(helpful);
        vo.setEvaluationCount(total);
        if (total > 0) {
            double rate = helpful * 100.0 / total;
            vo.setSatisfactionRate(Math.round(rate * 100) / 100.0);
        }
        return vo;
    }

    @Override
    public List<TopQuestionVO> topQuestions(int limit) {
        int n = limit <= 0 ? 10 : limit;
        List<ChatMessage> messages = chatMessageMapper.selectList(
                Wrappers.<ChatMessage>lambdaQuery().eq(ChatMessage::getRole, "user"));

        Map<String, Long> counter = new LinkedHashMap<>();
        for (ChatMessage m : messages) {
            String q = m.getContent() == null ? "" : m.getContent().replaceAll("\\s+", " ").trim();
            if (q.isEmpty()) {
                continue;
            }
            counter.merge(q, 1L, Long::sum);
        }

        return counter.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(n)
                .map(e -> {
                    TopQuestionVO vo = new TopQuestionVO();
                    vo.setContent(e.getKey());
                    vo.setCount(e.getValue());
                    return vo;
                })
                .toList();
    }
}
