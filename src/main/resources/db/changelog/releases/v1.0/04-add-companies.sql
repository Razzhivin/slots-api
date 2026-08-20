--liquibase formatted sql
--changeset author:4
CREATE TABLE companies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE api_keys (
    key_token VARCHAR(255) PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Добавляем внешние ключи с ограничением NOT NULL
ALTER TABLE resources ADD COLUMN company_id BIGINT REFERENCES companies(id) ON DELETE CASCADE;
ALTER TABLE bookings ADD COLUMN company_id BIGINT REFERENCES companies(id) ON DELETE CASCADE;

-- Создаем индексы
CREATE INDEX idx_resources_company ON resources(company_id);
CREATE INDEX idx_bookings_company ON bookings(company_id);
CREATE INDEX idx_api_keys_token ON api_keys(key_token) WHERE is_active = TRUE;
