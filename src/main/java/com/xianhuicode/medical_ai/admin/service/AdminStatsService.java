package com.xianhuicode.medical_ai.admin.service;

import com.xianhuicode.medical_ai.admin.dto.StatsOverviewVO;
import com.xianhuicode.medical_ai.admin.dto.TopQuestionVO;

import java.util.List;

public interface AdminStatsService {

    StatsOverviewVO overview();

    List<TopQuestionVO> topQuestions(int limit);
}
