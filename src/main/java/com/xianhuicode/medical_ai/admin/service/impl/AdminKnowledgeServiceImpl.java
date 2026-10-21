package com.xianhuicode.medical_ai.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianhuicode.medical_ai.admin.dto.KnowledgeReq;
import com.xianhuicode.medical_ai.admin.dto.KnowledgeVO;
import com.xianhuicode.medical_ai.admin.service.AdminKnowledgeService;
import com.xianhuicode.medical_ai.common.api.PageVO;
import com.xianhuicode.medical_ai.common.api.ResultCode;
import com.xianhuicode.medical_ai.common.audit.AuditLog;
import com.xianhuicode.medical_ai.common.audit.AuditLogMapper;
import com.xianhuicode.medical_ai.common.exception.BizException;
import com.xianhuicode.medical_ai.common.security.SecurityUtils;
import com.xianhuicode.medical_ai.knowledge.entity.KnowledgeEntry;
import com.xianhuicode.medical_ai.knowledge.mapper.KnowledgeEntryMapper;
import com.xianhuicode.medical_ai.knowledge.service.KnowledgeIndexer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 知识库管理（ADMIN-01）：新增/编辑/上下架/删除/列表/详情，发布/下架/删除时同步向量索引并留痕。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminKnowledgeServiceImpl implements AdminKnowledgeService {

    private final KnowledgeEntryMapper knowledgeEntryMapper;
    private final KnowledgeIndexer knowledgeIndexer;
    private final AuditLogMapper auditLogMapper;

    @Override
    public KnowledgeVO create(KnowledgeReq req) {
        KnowledgeEntry e = new KnowledgeEntry();
        e.setTitle(req.getTitle().trim());
        e.setContent(req.getContent());
        e.setSource(req.getSource());
        e.setCategory(req.getCategory());
        e.setStatus("PENDING");
        e.setVersion(1);
        e.setCreateBy(SecurityUtils.getCurrentUserId());
        knowledgeEntryMapper.insert(e);
        audit("KNOWLEDGE", e.getId(), "CREATE", "创建知识条目「" + e.getTitle() + "」");
        return toVO(e);
    }

    @Override
    public KnowledgeVO update(Long id, KnowledgeReq req) {
        KnowledgeEntry e = get(id);
        e.setTitle(req.getTitle().trim());
        e.setContent(req.getContent());
        e.setSource(req.getSource());
        e.setCategory(req.getCategory());
        e.setVersion((e.getVersion() == null ? 0 : e.getVersion()) + 1);
        e.setStatus("PENDING");
        knowledgeEntryMapper.updateById(e);
        audit("KNOWLEDGE", id, "UPDATE", "编辑知识条目「" + e.getTitle() + "」至 v" + e.getVersion());
        return toVO(e);
    }

    @Override
    public KnowledgeVO publish(Long id) {
        KnowledgeEntry e = get(id);
        e.setStatus("PUBLISHED");
        knowledgeEntryMapper.updateById(e);
        safeIndex(id);
        audit("KNOWLEDGE", id, "PUBLISH", "上架知识条目「" + e.getTitle() + "」");
        return toVO(e);
    }

    @Override
    public KnowledgeVO offline(Long id) {
        KnowledgeEntry e = get(id);
        e.setStatus("OFFLINE");
        knowledgeEntryMapper.updateById(e);
        safeRemove(id);
        audit("KNOWLEDGE", id, "OFFLINE", "下架知识条目「" + e.getTitle() + "」");
        return toVO(e);
    }

    @Override
    public void delete(Long id) {
        KnowledgeEntry e = get(id);
        knowledgeEntryMapper.deleteById(id);
        safeRemove(id);
        audit("KNOWLEDGE", id, "DELETE", "删除知识条目「" + e.getTitle() + "」");
    }

    @Override
    public PageVO<KnowledgeVO> list(long page, long size, String status, String category, String keyword) {
        boolean hasStatus = status != null && !status.isBlank();
        boolean hasCategory = category != null && !category.isBlank();
        boolean hasKeyword = keyword != null && !keyword.isBlank();

        LambdaQueryWrapper<KnowledgeEntry> qw = Wrappers.<KnowledgeEntry>lambdaQuery()
                .eq(hasStatus, KnowledgeEntry::getStatus, status)
                .eq(hasCategory, KnowledgeEntry::getCategory, category)
                .and(hasKeyword, w -> w
                        .like(KnowledgeEntry::getTitle, keyword)
                        .or()
                        .like(KnowledgeEntry::getContent, keyword))
                .orderByDesc(KnowledgeEntry::getId);

        Page<KnowledgeEntry> p = knowledgeEntryMapper.selectPage(new Page<>(page, size), qw);
        return new PageVO<>(
                p.getRecords().stream().map(this::toVO).toList(),
                p.getTotal(), p.getCurrent(), p.getSize());
    }

    @Override
    public KnowledgeVO detail(Long id) {
        return toVO(get(id));
    }

    private KnowledgeEntry get(Long id) {
        KnowledgeEntry e = knowledgeEntryMapper.selectById(id);
        if (e == null) {
            throw new BizException(ResultCode.ADMIN_KNOWLEDGE_NOT_FOUND);
        }
        return e;
    }

    private void audit(String targetType, Long targetId, String action, String detail) {
        AuditLog log = new AuditLog();
        log.setOperatorId(SecurityUtils.getCurrentUserId());
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setAction(action);
        log.setDetail(detail);
        auditLogMapper.insert(log);
    }

    /** 向量索引为派生的检索缓存，同步失败不阻断管理操作，可稍后 reindex 重建。 */
    private void safeIndex(Long id) {
        try {
            knowledgeIndexer.indexEntry(id);
        } catch (Exception ex) {
            log.warn("知识条目 #{} 向量索引同步失败（可稍后 reindex 重建）: {}", id, ex.getMessage());
        }
    }

    private void safeRemove(Long id) {
        try {
            knowledgeIndexer.removeEntry(id);
        } catch (Exception ex) {
            log.warn("知识条目 #{} 向量索引移除失败: {}", id, ex.getMessage());
        }
    }

    private KnowledgeVO toVO(KnowledgeEntry e) {
        KnowledgeVO vo = new KnowledgeVO();
        vo.setId(e.getId());
        vo.setTitle(e.getTitle());
        vo.setContent(e.getContent());
        vo.setSource(e.getSource());
        vo.setCategory(e.getCategory());
        vo.setStatus(e.getStatus());
        vo.setVersion(e.getVersion());
        vo.setCreateBy(e.getCreateBy());
        vo.setCreateTime(e.getCreateTime());
        vo.setUpdateTime(e.getUpdateTime());
        return vo;
    }
}
