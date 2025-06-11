package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.expert.domain.common.annotation.LogAdmin;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

@Aspect // 횡단 관심사(어플리케이션 전역에서 나타나는 관심사)를 정의한다는 어노테이션
@Component
@Slf4j
public class AdminLoggingAspect {

    private final ObjectMapper objectMapper;

    public AdminLoggingAspect(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // 적용 위치(Pointcut), 해당 위치에 있는 어노테이션을 가지고 있는 메서드인 동시에
    // 접근 범위가 public 인 모든 메서드의 호출 전, 후
    @Around("@annotation(org.example.expert.domain.common.annotation.LogAdmin) && execution(public * *(..))")
    public Object logAdmin(ProceedingJoinPoint joinPoint) throws Throwable{
        // 메서드 정보 추출
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // 어노테이션 값 추출
        LogAdmin logAdmin = signature.getMethod().getAnnotation(LogAdmin.class);
        String annotation = "default";
        if(logAdmin != null) {
            annotation = logAdmin.comment();
        }

        // 요청 데이터 추출
        String requestURI = "default";
        // String clientIp = "default";
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if(attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                requestURI = request.getRequestURI();
                // clientIp = request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.warn("No request attributes found");
        }

        // 요청 인자 추출
        Object[] args = joinPoint.getArgs();
        String argsString = "default";
        if(args != null && args.length > 0) {
            try {
                // JSON 으로 직렬화
                argsString = objectMapper.writeValueAsString(args);
            } catch (Exception e) {
                log.warn("Failed to serialize args");
                // JSON 변환 실패시 toString
                argsString = Arrays.toString(args);
            }
        }

        // 요청 시간(밀리초 제거)
        LocalDateTime requestTime = LocalDateTime.now().withNano(0);

        // 실행 전 로그
        log.info("[ADMIN_LOG] Before - {} ({} -> {}) | URI: {} | Args: {} | RequestTime: {}",
                annotation, className, methodName, requestURI, argsString, requestTime);

        Object result = null;
        long startTime = System.currentTimeMillis();
        try {
            // 메서드 실행
            result = joinPoint.proceed();
        } catch (Throwable e) {
            // 예외 발생 로그
            log.error("[ADMIN_LOG] Exception - {} ({} -> {}) | URI: {} | Error: {} | EndTime: {}",
                    annotation, className, methodName, requestURI, e.getMessage(), LocalDateTime.now().withNano(0),e);
            throw e;
        }

        // 응답 데이터 추출
        String resultJson = "default";
        if (result != null){
            try {
                // JSON 으로 직렬화
                resultJson = objectMapper.writeValueAsString(result);
            } catch (Exception e) {
                log.warn("Failed to serialize result: {}", result);
                // 직렬화 실패시 toString
                resultJson = Objects.toString(result);
            }
        }

        // 응답 시간(밀리초 제거)
        LocalDateTime endTime = LocalDateTime.now().withNano(0);

        // 실행 후 로그
        log.info("[ADMIN_LOG] After - {} ({} -> {}) | URI: {} | Result: {} | ResponseTime: {}",
                annotation, className, methodName, requestURI, resultJson, endTime);

        return result;
    }
}