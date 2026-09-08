package com.gancg.hikingmanageback.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
public class ApiAccessLogFilter extends OncePerRequestFilter {
    private static final int MAX_LOG_BODY_LEN = 3000;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (shouldSkip(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        long start = System.currentTimeMillis();
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long costMs = System.currentTimeMillis() - start;
            String requestBody = readBody(wrappedRequest.getContentAsByteArray(), wrappedRequest.getCharacterEncoding(), wrappedRequest.getContentType());
            String responseBody = readBody(wrappedResponse.getContentAsByteArray(), wrappedResponse.getCharacterEncoding(), wrappedResponse.getContentType());
            String query = wrappedRequest.getQueryString();
            String params = renderParams(wrappedRequest.getParameterMap());

            log.info(
                    "api access method={} uri={} query={} params={} requestBody={} status={} responseBody={} costMs={}",
                    wrappedRequest.getMethod(),
                    wrappedRequest.getRequestURI(),
                    query == null ? "" : query,
                    params,
                    requestBody,
                    wrappedResponse.getStatus(),
                    responseBody,
                    costMs
            );
            wrappedResponse.copyBodyToResponse();
        }
    }

    private boolean shouldSkip(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/error");
    }

    private String readBody(byte[] bytes, String encoding, String contentType) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        if (contentType != null && !isTextLike(contentType)) {
            return "[non-text body omitted]";
        }
        Charset charset = encoding == null ? StandardCharsets.UTF_8 : Charset.forName(encoding);
        String body = new String(bytes, charset).replaceAll("[\\r\\n]+", " ");
        if (body.length() > MAX_LOG_BODY_LEN) {
            return body.substring(0, MAX_LOG_BODY_LEN) + "...(truncated)";
        }
        return body;
    }

    private boolean isTextLike(String contentType) {
        return contentType.startsWith(MediaType.APPLICATION_JSON_VALUE)
                || contentType.startsWith(MediaType.APPLICATION_XML_VALUE)
                || contentType.startsWith(MediaType.TEXT_PLAIN_VALUE)
                || contentType.startsWith(MediaType.TEXT_HTML_VALUE)
                || contentType.startsWith(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                || MediaType.parseMediaTypes(contentType).stream()
                .anyMatch(mt -> "text".equalsIgnoreCase(mt.getType()));
    }

    private String renderParams(Map<String, String[]> parameterMap) {
        if (parameterMap == null || parameterMap.isEmpty()) {
            return "";
        }
        return parameterMap.entrySet().stream()
                .map(e -> e.getKey() + "=" + String.join(",", e.getValue()))
                .collect(Collectors.joining("&"));
    }
}
