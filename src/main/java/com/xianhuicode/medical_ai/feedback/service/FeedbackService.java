package com.xianhuicode.medical_ai.feedback.service;

import com.xianhuicode.medical_ai.feedback.dto.FeedbackReq;
import com.xianhuicode.medical_ai.feedback.dto.FeedbackVO;

import java.util.List;

/**
 * 反馈评价：提交评价/纠错（FB-01/02）、查看处理状态（FB-03）。
 */
public interface FeedbackService {

    FeedbackVO submit(Long userId, FeedbackReq req);

    List<FeedbackVO> list(Long userId);
}
