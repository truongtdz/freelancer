package com.freelancer.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freelancer.dto.response.ApiResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    // key = "ip:path" → Bucket
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("POST".equals(method) && isRateLimited(path)) {
            String ip = resolveIp(request);
            int limit = getLimit(path);
            long windowSeconds = 60;

            String key = ip + ":" + path;
            Bucket bucket = buckets.computeIfAbsent(key, k -> buildBucket(limit, windowSeconds));

            if (!bucket.tryConsume(1)) {
                log.warn("Rate limit exceeded: ip={} path={}", ip, path);
                response.setStatus(429);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                ApiResponse<Object> body = ApiResponse.error(429,
                        "Quá nhiều request, thử lại sau " + windowSeconds + " giây");
                response.getWriter().write(objectMapper.writeValueAsString(body));
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isRateLimited(String path) {
        return path.equals("/api/auth/login") || path.equals("/api/auth/forgot-password");
    }

    private int getLimit(String path) {
        return path.equals("/api/auth/forgot-password") ? 3 : 5;
    }

    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private Bucket buildBucket(int limit, long windowSeconds) {
        Bandwidth bandwidth = Bandwidth.classic(limit,
                Refill.intervally(limit, Duration.ofSeconds(windowSeconds)));
        return Bucket.builder().addLimit(bandwidth).build();
    }
}
