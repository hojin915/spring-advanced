package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    // 요청이 컨트롤러에 도착하기 이전
    // Admin 유저인지 확인 과정이 필요하면 이 부분에서
    // JwtFilter 에서 이미 걸러내고 있기 때문에 추가하지 않음
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);
        log.info("[Interceptor-Admin] preHandle - path: {} from IP: {}]",
                request.getRequestURI(), request.getRemoteAddr());

        return true;
    }

    @Override
    // 요청 처리 완료 후
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        long startTime = (Long) request.getAttribute("startTime");
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        log.info("[Interceptor-Admin] afterCompletion - path: {} completed. Status: {} | Duration: {}ms",
                request.getRequestURI(), response.getStatus(), duration);

        if(ex != null){
            log.error("[Interceptor-Admin] afterCompletion - path: {} finished with exception: {}",
                    request.getRequestURI(), ex.getMessage());
        }
    }
}