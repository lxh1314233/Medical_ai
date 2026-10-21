package com.xianhuicode.medical_ai.report.service;

import com.xianhuicode.medical_ai.ai.ocr.OcrGateway;
import com.xianhuicode.medical_ai.report.dto.IndicatorVO;
import com.xianhuicode.medical_ai.report.dto.ReportDetailVO;
import com.xianhuicode.medical_ai.report.entity.LabReference;
import com.xianhuicode.medical_ai.report.mapper.LabReferenceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 报告解读规则引擎：OCR 指标与 lab_reference 对照，判定 正常/轻度/显著/危急，生成综合小结。
 * 不调用 LLM——结构化对照表，可溯源、不编造（开发说明 §9.5）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportInterpreter {

    private static final String DISCLAIMER = "本解读基于指标参考值对照表自动生成，仅供健康参考，不构成诊断依据，请以医生诊断为准。";
    private static final String CRITICAL_SUMMARY = "检测结果发现危急值（见标注「危急」的指标），请立即前往急诊或就近医院就诊，切勿延误。";
    private static final Pattern NUMBER = Pattern.compile("-?\\d+(\\.\\d+)?");

    /** 异常分级集合（用于 abnormal_flags 标注） */
    public static final List<String> ABNORMAL_LEVELS = List.of("轻度", "显著", "危急");

    private final LabReferenceMapper labReferenceMapper;

    public ReportDetailVO interpret(List<OcrGateway.OcrIndicator> ocrIndicators) {
        List<LabReference> refs = labReferenceMapper.selectList(null);
        Map<String, LabReference> index = buildIndex(refs);

        List<IndicatorVO> indicators = new ArrayList<>();
        boolean critical = false;
        int abnormal = 0;
        int mild = 0;
        int significant = 0;

        for (OcrGateway.OcrIndicator ocr : ocrIndicators) {
            LabReference ref = match(ocr.item(), index);
            IndicatorVO item = interpretOne(ocr, ref);
            indicators.add(item);
            if (item.getLevel() == null) {
                continue;
            }
            switch (item.getLevel()) {
                case "危急" -> {
                    critical = true;
                    abnormal++;
                }
                case "显著" -> {
                    significant++;
                    abnormal++;
                }
                case "轻度" -> {
                    mild++;
                    abnormal++;
                }
                default -> {
                }
            }
        }

        ReportDetailVO vo = new ReportDetailVO();
        vo.setCriticalFlag(critical);
        vo.setIndicators(indicators);
        vo.setSummary(buildSummary(critical, indicators.size(), abnormal, mild, significant));
        vo.setDisclaimer(DISCLAIMER);
        return vo;
    }

    // ==================== 单项解读 ====================

    private IndicatorVO interpretOne(OcrGateway.OcrIndicator ocr, LabReference ref) {
        IndicatorVO vo = new IndicatorVO();
        vo.setItem(ocr.item());
        vo.setValue(ocr.value());
        vo.setUnit(ocr.unit());
        if (ref == null) {
            vo.setLevel("未收录");
            vo.setReason("该指标暂未收录参考范围，请咨询医生解读");
            return vo;
        }

        vo.setUnit(firstNonBlank(ocr.unit(), ref.getUnit()));
        vo.setRefRange(formatRange(ref));
        vo.setMeaning(ref.getMeaning());

        Double num = parseNumber(ocr.value());
        if (num == null) {
            vo.setLevel("无法判断");
            vo.setReason("无法解析数值，请人工核对报告");
            return vo;
        }

        String level = judgeLevel(num, ref);
        vo.setLevel(level);
        if ("正常".equals(level)) {
            vo.setReason(null);
        } else if (ABNORMAL_LEVELS.contains(level)) {
            boolean high = ref.getRefHigh() != null && num > ref.getRefHigh();
            vo.setReason(high ? ref.getHighReason() : ref.getLowReason());
        }
        return vo;
    }

    private String judgeLevel(double num, LabReference ref) {
        // 危急值优先
        if (ref.getCriticalLow() != null && num <= ref.getCriticalLow()) {
            return "危急";
        }
        if (ref.getCriticalHigh() != null && num >= ref.getCriticalHigh()) {
            return "危急";
        }
        Double low = ref.getRefLow();
        Double high = ref.getRefHigh();
        if (low != null && num < low) {
            return deviationRatio(low, high, num, false) >= 0.5 ? "显著" : "轻度";
        }
        if (high != null && num > high) {
            return deviationRatio(low, high, num, true) >= 0.5 ? "显著" : "轻度";
        }
        return "正常";
    }

    /** 偏离比例：|值-边界| / 参考范围宽度；无宽度时退化为相对边界偏离。 */
    private double deviationRatio(Double low, Double high, double num, boolean isHigh) {
        double range = (low == null || high == null) ? 0 : (high - low);
        double deviation = isHigh ? (num - high) : (low - num);
        if (range > 0) {
            return deviation / range;
        }
        double bound = isHigh ? (high == null ? 0 : high) : (low == null ? 0 : low);
        return bound == 0 ? deviation : deviation / Math.abs(bound);
    }

    // ==================== 匹配 ====================

    private Map<String, LabReference> buildIndex(List<LabReference> refs) {
        Map<String, LabReference> map = new HashMap<>();
        for (LabReference ref : refs) {
            put(map, ref.getItemName(), ref);
            if (ref.getAlias() != null) {
                for (String a : ref.getAlias().split("[,，]")) {
                    put(map, a, ref);
                }
            }
        }
        return map;
    }

    private void put(Map<String, LabReference> map, String key, LabReference ref) {
        String k = normalize(key);
        if (k != null && !k.isBlank()) {
            map.putIfAbsent(k, ref);
        }
    }

    private LabReference match(String item, Map<String, LabReference> index) {
        String norm = normalize(item);
        if (norm == null) {
            return null;
        }
        LabReference direct = index.get(norm);
        if (direct != null) {
            return direct;
        }
        for (Map.Entry<String, LabReference> e : index.entrySet()) {
            if (norm.contains(e.getKey()) || e.getKey().contains(norm)) {
                return e.getValue();
            }
        }
        return null;
    }

    private String normalize(String s) {
        if (s == null) {
            return null;
        }
        return s.replaceAll("\\s+", "").toLowerCase();
    }

    // ==================== 工具 ====================

    private Double parseNumber(String raw) {
        if (raw == null) {
            return null;
        }
        Matcher m = NUMBER.matcher(raw.trim());
        if (!m.find()) {
            return null;
        }
        try {
            return Double.parseDouble(m.group());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String formatRange(LabReference ref) {
        String low = ref.getRefLow() == null ? "" : trimNum(ref.getRefLow());
        String high = ref.getRefHigh() == null ? "" : trimNum(ref.getRefHigh());
        String unit = ref.getUnit() == null ? "" : " " + ref.getUnit();
        if (!low.isEmpty() && !high.isEmpty()) {
            return low + "~" + high + unit;
        }
        if (!low.isEmpty()) {
            return "≥" + low + unit;
        }
        if (!high.isEmpty()) {
            return "≤" + high + unit;
        }
        return null;
    }

    private String trimNum(Double d) {
        if (d == Math.floor(d) && !d.isInfinite()) {
            return String.valueOf(d.longValue());
        }
        return String.valueOf(d);
    }

    private String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        return b;
    }

    private String buildSummary(boolean critical, int total, int abnormal, int mild, int significant) {
        if (critical) {
            return CRITICAL_SUMMARY;
        }
        if (abnormal == 0) {
            return "共识别 " + total + " 项指标，均在参考范围内，未见明显异常。建议保持健康生活方式并定期体检。";
        }
        return "共识别 " + total + " 项指标，其中 " + abnormal + " 项超出参考范围（轻度 " + mild
                + " 项，显著 " + significant + " 项）。建议重点关注异常指标，结合自身症状及时咨询医生。";
    }
}
