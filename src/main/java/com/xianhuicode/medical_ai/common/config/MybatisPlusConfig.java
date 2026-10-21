package com.xianhuicode.medical_ai.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置：分页插件、Mapper 扫描。
 */
@Configuration
@MapperScan(basePackages = {
        "com.xianhuicode.medical_ai.auth.mapper",
        "com.xianhuicode.medical_ai.profile.mapper",
        "com.xianhuicode.medical_ai.chat.mapper",
        "com.xianhuicode.medical_ai.knowledge.mapper",
        "com.xianhuicode.medical_ai.triage.mapper",
        "com.xianhuicode.medical_ai.medication.mapper",
        "com.xianhuicode.medical_ai.report.mapper",
        "com.xianhuicode.medical_ai.feedback.mapper",
        "com.xianhuicode.medical_ai.common.audit",
        "com.xianhuicode.medical_ai.admin.mapper"
})
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.MYSQL);
        pagination.setMaxLimit(500L);
        interceptor.addInnerInterceptor(pagination);
        return interceptor;
    }
}
