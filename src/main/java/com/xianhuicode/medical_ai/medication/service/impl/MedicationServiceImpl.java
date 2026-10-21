package com.xianhuicode.medical_ai.medication.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xianhuicode.medical_ai.ai.guard.GuardResult;
import com.xianhuicode.medical_ai.ai.guard.ScopeGuard;
import com.xianhuicode.medical_ai.common.api.ResultCode;
import com.xianhuicode.medical_ai.common.exception.BizException;
import com.xianhuicode.medical_ai.common.util.CryptoUtil;
import com.xianhuicode.medical_ai.medication.dto.DrugInfoVO;
import com.xianhuicode.medical_ai.medication.dto.InteractionReq;
import com.xianhuicode.medical_ai.medication.dto.InteractionResultVO;
import com.xianhuicode.medical_ai.medication.dto.InteractionVO;
import com.xianhuicode.medical_ai.medication.entity.Drug;
import com.xianhuicode.medical_ai.medication.entity.DrugInteraction;
import com.xianhuicode.medical_ai.medication.entity.MedConsultRecord;
import com.xianhuicode.medical_ai.medication.mapper.DrugInteractionMapper;
import com.xianhuicode.medical_ai.medication.mapper.DrugMapper;
import com.xianhuicode.medical_ai.medication.mapper.MedConsultRecordMapper;
import com.xianhuicode.medical_ai.medication.service.MedicationService;
import com.xianhuicode.medical_ai.profile.entity.UserProfile;
import com.xianhuicode.medical_ai.profile.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 用药咨询实现：处方请求拦截（MED-06）→ 药典查询（MED-01）+ 档案个性化警示（MED-02/04）→ 相互作用比对（MED-03）→ 落库。
 * 药品事实一律来自结构化 drug 表，不调用 LLM，避免编造用法。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MedicationServiceImpl implements MedicationService {

    private static final String DISCLAIMER = "本结果仅供科普参考，不构成处方或用药建议，具体用法用量请以药品说明书或医师/药师指导为准。";
    private static final String NOT_FOUND_MSG = "未收录该药品，建议查阅药品说明书或咨询医师/药师。";
    private static final String INTERACTION_NOTE = "以上为常见已知药物相互作用，可能存在未收录的组合，具体请咨询医师或药师。";

    private final DrugMapper drugMapper;
    private final DrugInteractionMapper interactionMapper;
    private final MedConsultRecordMapper consultRecordMapper;
    private final UserProfileMapper profileMapper;
    private final ScopeGuard scopeGuard;
    private final CryptoUtil cryptoUtil;

    @Override
    public DrugInfoVO query(Long userId, String name) {
        String drugName = name == null ? "" : name.trim();
        if (drugName.isEmpty()) {
            throw new BizException(ResultCode.BIZ_PARAM_ERROR);
        }
        // MED-06 兜底：处方类请求硬拦截
        rejectIfGuardHit(drugName);

        Drug drug = findDrug(drugName);
        DrugInfoVO vo = new DrugInfoVO();
        if (drug == null) {
            vo.setFound(false);
            vo.setMessage(NOT_FOUND_MSG);
            vo.setDisclaimer(DISCLAIMER);
            saveRecord(userId, drugName, "未收录");
            return vo;
        }

        vo.setFound(true);
        vo.setGenericName(drug.getGenericName());
        vo.setBrandNames(drug.getBrandNames());
        vo.setIndications(drug.getIndications());
        vo.setDosage(drug.getDosage());
        vo.setContraindications(drug.getContraindications());
        vo.setAdverseReactions(drug.getAdverseReactions());
        vo.setStorage(drug.getStorage());
        vo.setSpecialPopulation(drug.getSpecialPopulation());
        vo.setWarnings(buildWarnings(drug, loadProfile(userId)));
        vo.setDisclaimer(DISCLAIMER);

        saveRecord(userId, drugName, "已收录：" + drug.getGenericName());
        return vo;
    }

    @Override
    public InteractionResultVO checkInteractions(Long userId, InteractionReq req) {
        List<String> drugs = req.getDrugs().stream()
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .distinct()
                .toList();

        InteractionResultVO vo = new InteractionResultVO();
        if (drugs.size() < 2) {
            vo.setInteractions(List.of());
            vo.setNote("需至少 2 种药品才能进行相互作用检查。" + INTERACTION_NOTE);
            saveRecord(userId, String.join("、", drugs), "药品不足 2 种");
            return vo;
        }

        // MED-06 兜底
        rejectIfGuardHit(String.join(" ", drugs));

        List<InteractionVO> hits = new ArrayList<>();
        for (int i = 0; i < drugs.size(); i++) {
            for (int j = i + 1; j < drugs.size(); j++) {
                DrugInteraction hit = findInteraction(drugs.get(i), drugs.get(j));
                if (hit != null) {
                    hits.add(toInteractionVO(hit));
                }
            }
        }
        vo.setInteractions(hits);
        vo.setNote(INTERACTION_NOTE);

        saveRecord(userId, String.join("、", drugs), "命中 " + hits.size() + " 条相互作用");
        return vo;
    }

    // ==================== 查询 ====================

    private Drug findDrug(String name) {
        Drug byGeneric = drugMapper.selectOne(
                Wrappers.<Drug>lambdaQuery().eq(Drug::getGenericName, name));
        if (byGeneric != null) {
            return byGeneric;
        }
        return drugMapper.selectOne(
                Wrappers.<Drug>lambdaQuery().like(Drug::getBrandNames, name));
    }

    private DrugInteraction findInteraction(String a, String b) {
        return interactionMapper.selectOne(
                Wrappers.<DrugInteraction>lambdaQuery()
                        .and(w -> w.and(x -> x.eq(DrugInteraction::getDrugA, a).eq(DrugInteraction::getDrugB, b))
                                .or(y -> y.eq(DrugInteraction::getDrugA, b).eq(DrugInteraction::getDrugB, a))));
    }

    // ==================== 个性化警示（MED-02/04） ====================

    private List<String> buildWarnings(Drug drug, UserProfile profile) {
        List<String> warnings = new ArrayList<>();
        if (drug.getSpecialPopulation() != null && !drug.getSpecialPopulation().isBlank()) {
            warnings.add("特殊人群：" + drug.getSpecialPopulation());
        }
        if (profile == null) {
            return warnings;
        }

        Integer age = ageOf(profile.getBirthDate());
        if (age != null) {
            if (age < 14) {
                warnings.add("儿童用药请遵医嘱，按体重/年龄计算剂量，勿自行给药");
            } else if (age >= 65) {
                warnings.add("老年患者用药需谨慎，注意肝肾功能，建议遵医嘱");
            } else {
                warnings.add("结合您的年龄（" + age + " 岁），具体用量请以说明书或医嘱为准");
            }
        }

        String allergy = safeDecrypt(profile.getAllergyHistory());
        if (allergy != null && !allergy.isBlank() && allergy.contains(drug.getGenericName())) {
            warnings.add("⚠️ 您的过敏史提到「" + drug.getGenericName() + "」，可能存在过敏风险，请勿自行服用并咨询医师");
        }

        String hist = joinNonBlank(safeDecrypt(profile.getPastHistory()), safeDecrypt(profile.getMedicationHistory()));
        if (hist.contains("孕妇") || hist.contains("怀孕")) {
            warnings.add("孕期用药需谨慎，请务必在医师指导下使用");
        }
        if (hist.contains("哺乳")) {
            warnings.add("哺乳期用药需谨慎，请咨询医师后再使用");
        }
        if (hist.contains("肝") && (hist.contains("不全") || hist.contains("衰竭") || hist.contains("功能") || hist.contains("异常"))
                || hist.contains("肾") && (hist.contains("不全") || hist.contains("衰竭") || hist.contains("功能") || hist.contains("异常"))) {
            warnings.add("肝肾功能异常者慎用，请遵医嘱调整剂量");
        }
        return warnings;
    }

    private UserProfile loadProfile(Long userId) {
        return profileMapper.selectOne(
                Wrappers.<UserProfile>lambdaQuery().eq(UserProfile::getUserId, userId));
    }

    private Integer ageOf(LocalDate birthDate) {
        if (birthDate == null) {
            return null;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private String safeDecrypt(String cipher) {
        try {
            return cryptoUtil.decrypt(cipher);
        } catch (Exception e) {
            log.warn("档案字段解密失败，跳过个性化警示: {}", e.getMessage());
            return null;
        }
    }

    private String joinNonBlank(String a, String b) {
        StringBuilder sb = new StringBuilder();
        if (a != null && !a.isBlank()) {
            sb.append(a);
        }
        if (b != null && !b.isBlank()) {
            if (!sb.isEmpty()) {
                sb.append(" ");
            }
            sb.append(b);
        }
        return sb.toString();
    }

    // ==================== 其它 ====================

    private void rejectIfGuardHit(String input) {
        Optional<GuardResult> hit = scopeGuard.check(input);
        if (hit.isPresent()) {
            throw new BizException(ResultCode.BIZ_PARAM_ERROR, hit.get().reply());
        }
    }

    private InteractionVO toInteractionVO(DrugInteraction d) {
        InteractionVO vo = new InteractionVO();
        vo.setDrugA(d.getDrugA());
        vo.setDrugB(d.getDrugB());
        vo.setRiskLevel(d.getRiskLevel());
        vo.setDescription(d.getDescription());
        return vo;
    }

    private void saveRecord(Long userId, String query, String result) {
        MedConsultRecord record = new MedConsultRecord();
        record.setUserId(userId);
        record.setQuery(query);
        record.setResult(result);
        consultRecordMapper.insert(record);
    }
}
