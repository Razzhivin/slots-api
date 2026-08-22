--liquibase formatted sql
--changeset author:5
--comment: Инициализация тестовой компании и API-ключа для b2b-тестирования

INSERT INTO companies (id, name)
VALUES (1, 'Клиника Мед-Эксперт')
ON CONFLICT (id) DO NOTHING;

INSERT INTO api_keys (key_token, company_id, is_active)
VALUES ('sk_test_token_42', 1, true)
ON CONFLICT (key_token) DO NOTHING;