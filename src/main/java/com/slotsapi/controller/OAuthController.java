package com.slotsapi.controller;

import com.slotsapi.model.ApiKey;
import com.slotsapi.model.Company;
import com.slotsapi.service.AmoAuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
@Slf4j
public class OAuthController {

    private final AmoAuthService amoAuthService;

    @Value("${amocrm.client-id}")
    private String clientId;

    /**
     * Инициирует процесс авторизации: генерирует state и перенаправляет на amoCRM.
     * Этот эндпоинт будет использоваться из виджета для открытия окна авторизации.
     */
    @GetMapping("/authorize")
    public String authorize(HttpSession session) {
        String state = UUID.randomUUID().toString();
        session.setAttribute("oauth_state", state);
        String redirectUrl = "https://www.amocrm.ru/oauth?client_id=" + clientId +
                "&state=" + state +
                "&mode=popup";
        log.info("Redirecting to amoCRM OAuth with state: {}", state);
        return "redirect:" + redirectUrl;
    }

    /**
     * Callback, куда amoCRM перенаправляет после авторизации пользователя.
     * Выполняет обмен кода на токены, регистрирует компанию и сохраняет токены.
     */
    @GetMapping("/callback")
    public ResponseEntity<?> callback(
            @RequestParam("code") String code,
            @RequestParam("referer") String referer,
            @RequestParam(value = "state", required = false) String state,
            HttpSession session) {

        log.info("Received OAuth callback from referer: {}", referer);

        // 1. Проверка state (защита от CSRF)
        String sessionState = (String) session.getAttribute("oauth_state");
        if (state == null || !state.equals(sessionState)) {
            log.warn("Invalid state parameter: expected {}, got {}", sessionState, state);
            return ResponseEntity.status(401).body("Invalid state parameter");
        }
        session.removeAttribute("oauth_state");

        try {
            // 2. Обработка OAuth-потока: обмен кода, регистрация компании, сохранение токенов
            Company company = amoAuthService.handleOAuthCallback(code, referer);

            // 3. Успешный ответ с показом API-ключа
            String apiKey = company.getApiKeys().stream()
                    .filter(ApiKey::isActive)
                    .findFirst()
                    .map(ApiKey::getKeyToken)
                    .orElse("Не найден активный API-ключ");

            String htmlResponse = """
                    <html>
                        <head><title>Интеграция установлена</title></head>
                        <body style="font-family: Arial, sans-serif; text-align: center; padding: 50px;">
                            <h1>✅ Интеграция успешно установлена!</h1>
                            <p>Компания: <strong>%s</strong></p>
                            <p>Ваш API-ключ для доступа к Slots API:</p>
                            <code style="background: #f4f4f4; padding: 10px; display: inline-block; border-radius: 5px;">%s</code>
                            <p style="margin-top: 20px;">Сохраните этот ключ, он понадобится для работы с API.</p>
                            <script>setTimeout(() => window.close(), 10000);</script>
                        </body>
                    </html>
                    """.formatted(company.getName(), apiKey);

            return ResponseEntity.ok().body(htmlResponse);

        } catch (Exception e) {
            log.error("OAuth callback processing failed", e);
            return ResponseEntity.status(500).body("Ошибка установки интеграции: " + e.getMessage());
        }
    }
}