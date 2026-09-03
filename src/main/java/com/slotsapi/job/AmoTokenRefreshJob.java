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

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000; // 2 секунды между попытками

    /**
     * Запускается каждые 30 минут.
     * Находит все токены, срок действия которых истекает в ближайшие 10 минут, и обновляет их.
     * При сбое выполняет до 3 повторных попыток с задержкой.
     */
    @Scheduled(cron = "0 */30 * * * *")
    @Transactional
    public void refreshExpiringTokens() {
        Instant threshold = Instant.now().plus(10, ChronoUnit.MINUTES);
        List<AmoToken> tokens = amoTokenRepository.findExpiredBefore(threshold);

        if (tokens.isEmpty()) {
            log.debug("No tokens need refreshing");
            return;
        }

        log.info("Found {} tokens to refresh", tokens.size());

        for (AmoToken token : tokens) {
            boolean success = refreshWithRetry(token);
            if (!success) {
                log.error("❌ Failed to refresh tokens for company {} after {} attempts",
                        token.getCompany().getId(), MAX_RETRIES);
            }
        }
    }

    /**
     * Выполняет обновление токена с повторными попытками.
     *
     * @param token объект токена для обновления
     * @return true, если обновление успешно, иначе false
     */
    private boolean refreshWithRetry(AmoToken token) {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                AmoTokenResponse response = amoApiClient.refreshTokens(
                        token.getRefreshToken(),
                        token.getSubdomain()
                );

                // Обновляем токены в БД
                token.setAccessToken(response.getAccessToken());
                token.setRefreshToken(response.getRefreshToken());
                token.setExpiresAt(Instant.now().plusSeconds(response.getExpiresIn()));
                amoTokenRepository.save(token);

                log.info("✅ Refreshed tokens for company: {}", token.getCompany().getId());
                return true;

            } catch (Exception e) {
                attempt++;
                log.warn("⚠️ Refresh attempt {} failed for company {}: {}",
                        attempt, token.getCompany().getId(), e.getMessage());

                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Retry interrupted for company {}", token.getCompany().getId());
                        return false;
                    }
                }
            }
        }
        return false;
    }
}