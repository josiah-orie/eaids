package com.feadev.eaids.config;

import com.feadev.eaids.scouting.security.ScoutProfileCompletionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final ScoutProfileCompletionInterceptor scoutProfileCompletionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(scoutProfileCompletionInterceptor)
                .addPathPatterns("/api/v1/scouts/**", "/api/v1/scouting/**");
    }
}
