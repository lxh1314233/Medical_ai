package com.xianhuicode.medical_ai.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianhuicode.medical_ai.admin.dto.AuditLogVO;
import com.xianhuicode.medical_ai.admin.dto.FeedbackReviewVO;
import com.xianhuicode.medical_ai.admin.service.AdminReviewService;
import com.xianhuicode.medical_ai.common.api.PageVO;
import com.xianhuicode.medical_ai.common.api.ResultCode;
import com.xianhuicode.medical_ai.common.audit.AuditLog;
import com.xianhuicode.medical_ai.common.audit.AuditLogMapper;
import com.xianhuicode.medical_ai.common.exception.BizException;
import com.xianhuicode.medical_ai.common.security.SecurityUtils;
import com.xianhuicode.medical_ai.feedback.entity.Feedback;
import com.xianhuicode.medical_ai.feedback.mapper.FeedbackMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 内容审核（ADMIN-02）：反馈审核队列 + 处理；审核留痕（纠错 + 守卫命中高风险问答）查看。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminReviewServiceImpl implements AdminReviewService {

    private final FeedbackMapper feedbackMapper;
    private final AuditLogMapper auditLogMapper;

    @Override
    public PageVO<FeedbackReviewVO> listFeedback(long page, long size, String type, String status) {
        boolean hasType = type != null && !type.isBlank();
        boolean hasStatus = status != null && !status.isBlank();
        LambdaQueryWrapper<Feedback> qw = Wrappers.<Feedback>lambdaQuery()
                .eq(hasType, Feedback::getType, type)
                .eq(hasStatus, Feedback::getStatus, status)
                .orderByDesc(Feedback::getId);
        Page<Feedback> p = feedbackMapper.selectPage(new Page<>(page, size), qw);
        return new PageVO<>(
                p.getRecords().stream().map(this::toFeedbackVO).toList(),
                p.getTotal(), p.getCurrent(), p.getSize());
    }

    @Override
    public void processFeedback(Long id) {
        Feedback f = feedbackMapper.selectById(id);
        if (f == null) {
            throw new BizException(ResultCode.ADMIN_FEEDBACK_NOT_FOUND);
        }
        f.setStatus("PROCESSED");
        feedbackMapper.updateById(f);

        AuditLog log = new AuditLog();
        log.setOperatorId(SecurityUtils.getCurrentUserId());
        log.setTargetType("FEEDBACK");
        log.setTargetId(id);
        log.setAction("REVIEW");
        log.setDetail("处理反馈 #" + id);
        auditLogMapper.insert(log);
    }

    @Override
    public PageVO<AuditLogVO> listAuditLogs(long page, long size) {
        Page<AuditLog> p = auditLogMapper.selectPage(new Page<>(page, size),
                Wrappers.<AuditLog>lambdaQuery().orderByDesc(AuditLog::getId));
        return new PageVO<>(
                p.getRecords().stream().map(this::toAuditVO).toList(),
                p.getTotal(), p.getCurrent(), p.getSize());
    }

    private FeedbackReviewVO toFeedbackVO(Feedback f) {
        FeedbackReviewVO vo = new FeedbackReviewVO();
        vo.setId(f.getId());
        vo.setUserId(f.getUserId());
        vo.setMessageId(f.getMessageId());
        vo.setType(f.getType());
        vo.setContent(f.getContent());
        vo.setStatus(f.getStatus());
        vo.setCreateTime(f.getCreateTime());
        return vo;
    }

    private AuditLogVO toAuditVO(AuditLog l) {
        AuditLogVO vo = new AuditLogVO();
        vo.setId(l.getId());
        vo.setOperatorId(l.getOperatorId());
        vo.setTargetType(l.getTargetType());
        vo.setTargetId(l.getTargetId());
        vo.setAction(l.getAction());
        vo.setDetail(l.getDetail());
        vo.setCreateTime(l.getCreateTime());
        return vo;
    }
}
