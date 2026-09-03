package com.slotsapi.security;

import com.slotsapi.repository.ApiKeyRepository;
import com.slotsapi.repository.CompanyRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    private final ApiKeyRepository apiKeyRepository;
    private final CompanyRepository companyRepository;

    public ApiKeyInterceptor(ApiKeyRepository apiKeyRepository, CompanyRepository companyRepository) {
        this.apiKeyRepository = apiKeyRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid Authorization header");
            return false;
        }

        String token = authHeader.substring(7);
        Optional<Long> companyIdOpt = apiKeyRepository.findCompanyIdByToken(token);

        if (companyIdOpt.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or inactive API key");
            return false;
        }

        Optional<Boolean> isActiveOpt = companyRepository.findActiveStatusById(companyIdOpt.get());
        if (isActiveOpt.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Company account is inactive");
            return false;
        }

        // Прокидываем ID компании в атрибуты текущего HTTP-запроса
        request.setAttribute("CURRENT_COMPANY_ID", companyIdOpt.get());
        return true;
    }
}