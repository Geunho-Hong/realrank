package com.project.realrank.common.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("execution(* com.project.realrank.product.controller..*Controller.*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String clientIp = LoggingData.getClientIp(request);
        String headers = LoggingData.getAllHeaders(request);
        String method = request.getMethod();
        String requestUrl = request.getRequestURI();
        String params = LoggingData.getAllParams(request);

        try {
            return joinPoint.proceed();
        }  finally {
            log.info("Request: Remote IP: {}, Headers: {}, Method: {}, URI: {}, Parameter: {}",
                    clientIp,
                    headers,
                    method,
                    requestUrl,
                    params
            );
        }
    }

}
