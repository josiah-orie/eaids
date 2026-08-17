package com.feadev.eaids.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feadev.eaids.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private List<String> allowedOrigins;

    private final String[] PERMIT_FRONTEND_PAGES = {
            "/*.html",
            "/favicon.svg",
            "/assets/**",
            "/images/**",
            "/js/**"
    };
//    String[] pages = PERMIT_FRONTEND_PAGES.lines()
//            .filter(s -> !s.isBlank())
//            .toArray(String[]::new);

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(PERMIT_FRONTEND_PAGES).permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Role-based access (method-level @PreAuthorize handles fine-grained control)
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/dashboard/athlete").hasRole("ATHLETE")
                        .requestMatchers("/api/v1/dashboard/coach").hasRole("COACH")
                        .requestMatchers("/api/v1/dashboard/admin").hasRole("ADMIN")
                        .requestMatchers("/api/v1/reports/**").hasAnyRole("ADMIN", "COACH")
                        .requestMatchers("/api/v1/athletes/me/**").hasRole("ATHLETE")
                        .requestMatchers("/api/v1/athletes/**").authenticated()
                        .requestMatchers("/api/v1/coaches/**").hasRole("COACH")
                        .requestMatchers("/api/v1/scouts/**").hasRole("SCOUT")
                        // Phase 3 - Analytics & Scouting
                        .requestMatchers("/api/v1/analytics/**").hasAnyRole("ADMIN", "COACH")
                        .requestMatchers("/api/v1/scouting/**").hasRole("SCOUT")
                        .requestMatchers("/api/v1/national-rankings/**").authenticated()
                        .requestMatchers("/api/v1/development-plans/**").authenticated()
                        .requestMatchers("/api/v1/video-library/**").authenticated()
                        .requestMatchers("/api/v1/camps/**").authenticated()
                        .requestMatchers("/api/v1/events/*/register/guest", "/api/v1/events/*/register/guest/cancel").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/events").permitAll()
                        .requestMatchers("/api/v1/events/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            Map<String, Object> body = Map.of(
                                    "timestamp", Instant.now().toString(),
                                    "path", request.getRequestURI(),
                                    "status", 401,
                                    "message", "Unauthorized: Authentication is required to access this resource"
                            );
                            objectMapper.writeValue(response.getOutputStream(), body);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            Map<String, Object> body = Map.of(
                                    "timestamp", Instant.now().toString(),
                                    "status", 403,
                                    "message", "Forbidden: You do not have permission to access this resource"
                            );
                            objectMapper.writeValue(response.getOutputStream(), body);
                        })
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
