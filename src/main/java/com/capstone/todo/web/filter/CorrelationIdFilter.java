package com.capstone.todo.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String MDC_KEY = "correlationId";

    private static final int MAX_CORRELATION_ID_LENGTH = 64;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String correlationId = normalizeCorrelationId(request.getHeader(CORRELATION_ID_HEADER));
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        MDC.put(MDC_KEY, correlationId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private static String normalizeCorrelationId(String correlationId) {
        if (correlationId == null) {
            return null;
        }

        String trimmed = correlationId.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_CORRELATION_ID_LENGTH) {
            return null;
        }

        for (int i = 0; i < trimmed.length(); i++) {
            char ch = trimmed.charAt(i);
            if (Character.isISOControl(ch)) {
                return null;
            }
            if (!(Character.isLetterOrDigit(ch) || ch == '-' || ch == '_' || ch == '.' )) {
                return null;
            }
        }

        return trimmed;
    }
}
