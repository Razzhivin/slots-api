package com.slotsapi.job;

import com.slotsapi.client.AmoApiClient;
import com.slotsapi.dto.amocrm.AmoTokenResponse;
import com.slotsapi.model.AmoToken;
import com.slotsapi.repository.AmoTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AmoTokenRefreshJob {

    private final AmoTokenRepository amoTokenRepository;
    private final AmoApiClient amoApiClient;

    @Scheduled(cron = "0 */30 * * * *") // Каждые 30 минут
    @Transactional
    public void refreshExpiringTokens() {
        Instant threshold = Instant.now().plus(10, ChronoUnit.MINUTES);
        List<AmoToken> tokens = amoTokenRepository.findExpiredBefore(threshold);

        log.info("Found {} tokens to refresh", tokens.size());

        for (AmoToken token : tokens) {
            try {
                AmoTokenResponse response = amoApiClient.refreshTokens(
                        token.getRefreshToken(),
                        token.getSubdomain()
                );

                token.setAccessToken(response.getAccessToken());
                token.setRefreshToken(response.getRefreshToken());
                token.setExpiresAt(Instant.now().plusSeconds(response.getExpiresIn()));
                amoTokenRepository.save(token);

                log.info("Refreshed tokens for company: {}", token.getCompany().getId());
            } catch (Exception e) {
                log.error("Failed to refresh tokens for company: {}",
                        token.getCompany().getId(), e);
            }
        }
    }
}