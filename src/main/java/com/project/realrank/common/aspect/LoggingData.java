package com.project.realrank.common.aspect;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;

import java.util.*;

public class LoggingData {

    private static final Gson gson = new Gson();

    public static String getAllHeaders(HttpServletRequest request) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, Collections.list(request.getHeaders(headerName)));
        }
        return gson.toJson(headers);
    }

    public static String getAllParams(HttpServletRequest request) {
        Map<String, List<String>> params = new LinkedHashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, Arrays.asList(v)));
        return gson.toJson(params);
    }

    public static String getClientIp(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (header != null && !header.isBlank()) {
            String[] ips = header.split(",");
            return ips[0].trim();
        }
        String fallback = request.getHeader("X-Real-IP");
        if (fallback != null && !fallback.isBlank()) {
            return fallback.trim();
        }
        return request.getRemoteAddr();
    }

}
