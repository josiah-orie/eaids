package com.feadev.eaids.config;

import com.feadev.eaids.scouting.security.ScoutProfileCompletionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final ScoutProfileCompletionInterceptor scoutProfileCompletionInterceptor;

    @Value("${storage.local.base-path:./storage}")
    private String storageBasePath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(scoutProfileCompletionInterceptor)
                .addPathPatterns("/api/v1/scouts/**", "/api/v1/scouting/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path absolutePath = Paths.get(storageBasePath).toAbsolutePath().normalize();
        String resourceLocation = "file:" + absolutePath + "/";

        registry.addResourceHandler("/storage/**")
                .addResourceLocations(resourceLocation)
                .setCacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic());
    }
}
