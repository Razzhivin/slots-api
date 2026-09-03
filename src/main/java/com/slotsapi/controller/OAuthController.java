package com.slotsapi.controller;

import com.slotsapi.service.AmoAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
@Slf4j
public class OAuthController {

    private final AmoAuthService amoAuthService;

    @GetMapping("/callback")
    public ResponseEntity<?> callback(
            @RequestParam("code") String code,
            @RequestParam("referer") String referer,
            @RequestParam(value = "state", required = false) String state) {

        log.info("OAuth callback: referer={}", referer);
        try {
            amoAuthService.handleOAuthCallback(code, referer);
            String successHtml = """
                <html><body><h1>✅ Интеграция установлена!</h1>
                <p>Вы можете закрыть это окно.</p>
                <script>setTimeout(window.close, 3000);</script>
                </body></html>
                """;
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(successHtml);
        } catch (Exception e) {
            log.error("OAuth callback error", e);
            return ResponseEntity.status(500).body("Ошибка установки интеграции");
        }
    }
}