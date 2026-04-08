package com.team2.activity.config;

import com.team2.activity.command.domain.entity.enums.ActivityType;
import com.team2.activity.command.domain.entity.enums.MailStatus;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Spring MVC의 요청 파라미터 변환 규칙을 등록하는 설정 클래스다.
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 커스텀 Converter를 Spring에 등록해 @RequestParam의 enum 변환을 확장한다.
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 쿼리 파라미터 문자열을 MailStatus enum으로 변환하는 컨버터를 등록한다.
        registry.addConverter(new StringToMailStatusConverter());
        // 쿼리 파라미터 문자열을 ActivityType enum으로 변환하는 컨버터를 등록한다.
        registry.addConverter(new StringToActivityTypeConverter());
    }

    // "sent", "SENT" 등 대소문자 구분 없이 MailStatus로 변환하는 컨버터다.
    private static class StringToMailStatusConverter implements Converter<String, MailStatus> {
        // 기본 Enum.valueOf() 대신 MailStatus.from()을 호출해 대소문자를 모두 허용한다.
        @Override
        public MailStatus convert(String source) {
            return MailStatus.from(source);
        }
    }

    // "meeting", "MEETING" 등 대소문자 구분 없이 ActivityType으로 변환하는 컨버터다.
    private static class StringToActivityTypeConverter implements Converter<String, ActivityType> {
        // 기본 Enum.valueOf() 대신 ActivityType.from()을 호출해 대소문자를 모두 허용한다.
        @Override
        public ActivityType convert(String source) {
            return ActivityType.from(source);
        }
    }
}
