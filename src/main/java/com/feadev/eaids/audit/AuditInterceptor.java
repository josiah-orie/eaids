package com.feadev.eaids.audit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * Utility component to extract audit-relevant information (IP address, User-Agent)
 * from the current HTTP request context.
 */
@Component
public class AuditInterceptor {

    /**
     * Extracts the client IP address from the current request.
     * Checks X-Forwarded-For header first (for proxied requests), then falls back
     * to the remote address from the servlet request.
     *
     * @return the client IP address, or "unknown" if not available
     */
    public String getClientIpAddress() {
        return getCurrentRequest()
                .map(this::extractIpAddress)
                .orElse("unknown");
    }

    /**
     * Extracts the User-Agent header from the current request.
     *
     * @return the User-Agent string, or "unknown" if not available
     */
    public String getUserAgent() {
        return getCurrentRequest()
                .map(request -> request.getHeader("User-Agent"))
                .map(ua -> ua.length() > 512 ? ua.substring(0, 512) : ua)
                .orElse("unknown");
    }

    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // X-Forwarded-For can contain multiple IPs; the first is the original client
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }

        return request.getRemoteAddr();
    }

    private Optional<HttpServletRequest> getCurrentRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(attrs -> attrs instanceof ServletRequestAttributes)
                .map(attrs -> ((ServletRequestAttributes) attrs).getRequest());
    }
}
