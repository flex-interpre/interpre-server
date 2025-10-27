package com.flex.interpre.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            logRequest(wrappedRequest);
            logResponse(wrappedResponse, duration);
            
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        StringBuilder logMessage = new StringBuilder("\n========== REQUEST ==========\n");
        
        logMessage.append("Method: ").append(request.getMethod()).append("\n");
        logMessage.append("URI: ").append(request.getRequestURI()).append("\n");
        
        String queryString = request.getQueryString();
        if (queryString != null) {
            logMessage.append("Query String: ").append(queryString).append("\n");
        }
        
        logMessage.append("Remote Address: ").append(request.getRemoteAddr()).append("\n");
        
        Map<String, String> headers = getHeaders(request);
        logMessage.append("Headers: ").append(headers).append("\n");
        
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            String body = new String(content, StandardCharsets.UTF_8);
            logMessage.append("Body: ").append(body).append("\n");
        }
        
        logMessage.append("============================");
        
        log.info(logMessage.toString());
    }

    private void logResponse(ContentCachingResponseWrapper response, long duration) {
        StringBuilder logMessage = new StringBuilder("\n========== RESPONSE ==========\n");
        
        logMessage.append("Status: ").append(response.getStatus()).append("\n");
        logMessage.append("Duration: ").append(duration).append("ms\n");
        
        response.getHeaderNames().forEach(headerName -> 
            logMessage.append("Header - ").append(headerName)
                      .append(": ").append(response.getHeader(headerName)).append("\n")
        );
        
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            String body = new String(content, StandardCharsets.UTF_8);
            logMessage.append("Body: ").append(body).append("\n");
        }
        
        logMessage.append("=============================");
        
        log.info(logMessage.toString());
    }

    private Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        
        return headers;
    }
}
