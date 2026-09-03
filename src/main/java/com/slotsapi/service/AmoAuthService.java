package com.slotsapi.service;

import com.slotsapi.client.AmoApiClient;
import com.slotsapi.dto.amocrm.AmoTokenResponse;
import com.slotsapi.model.AmoToken;
import com.slotsapi.model.ApiKey;
import com.slotsapi.model.Company;
import com.slotsapi.repository.AmoTokenRepository;
import com.slotsapi.repository.ApiKeyRepository;
import com.slotsapi.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AmoAuthService {

    private final CompanyRepository companyRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final AmoTokenRepository amoTokenRepository;
    private final AmoApiClient amoApiClient;

    @Transactional
    public Company handleOAuthCallback(String code, String referer) {
        String subdomain = extractSubdomain(referer);

        // Обмен кода на токены
        AmoTokenResponse tokenResponse = amoApiClient.exchangeCodeForTokens(code, subdomain);

        Long accountId = amoApiClient.getAccountId(subdomain, tokenResponse.getAccessToken());

        // Найти или создать компанию
        Company company = companyRepository.findByAmocrmSubdomain(subdomain)
                .orElseGet(() -> createCompany(subdomain, accountId));

        // Сохранить токены
        AmoToken token = amoTokenRepository.findByCompanyAndAccountId(company, accountId)
                .orElse(new AmoToken());
        token.setCompany(company);
        token.setAccountId(accountId);
        token.setSubdomain(subdomain);
        token.setAccessToken(tokenResponse.getAccessToken());
        token.setRefreshToken(tokenResponse.getRefreshToken());
        // Используем UTC
        token.setExpiresAt(Instant.now().plusSeconds(tokenResponse.getExpiresIn()));
        amoTokenRepository.save(token);

        return company;
    }

    private Company createCompany(String subdomain, Long accountId) {
        Company company = new Company();
        company.setName("Клиника " + subdomain);
        company.setAmocrmSubdomain(subdomain);
        company.setAmocrmAccountId(accountId);
        company.setActive(true);
        company = companyRepository.save(company);

        ApiKey apiKey = new ApiKey();
        apiKey.setKeyToken("sk_" + UUID.randomUUID().toString().replace("-", ""));
        apiKey.setCompany(company);
        apiKey.setActive(true);
        apiKeyRepository.save(apiKey);

        return company;
    }

    private String extractSubdomain(String referer) {
        return referer.split("\\.")[0];
    }
}
