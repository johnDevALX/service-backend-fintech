package com.ekene.servicebackendfintech.config;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets;
    private final Supplier<Bucket> bucketSupplier;

    public RateLimitFilter(Map<String, Bucket> buckets, Supplier<Bucket> bucketSupplier) {
        this.buckets = buckets;
        this.bucketSupplier = bucketSupplier;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String ip = request.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(ip, k -> bucketSupplier.get());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.getWriter().write("Too Many Requests");
        }
    }
}
