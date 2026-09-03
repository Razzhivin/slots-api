-- Добавляем поля для интеграции с amoCRM
ALTER TABLE companies ADD COLUMN amocrm_subdomain VARCHAR(255) UNIQUE;
ALTER TABLE companies ADD COLUMN amocrm_account_id BIGINT;
ALTER TABLE companies ADD COLUMN is_active BOOLEAN DEFAULT TRUE;

-- Индекс для быстрого поиска по subdomain
CREATE INDEX idx_companies_amocrm_subdomain ON companies(amocrm_subdomain);

CREATE TABLE amocrm_tokens (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    account_id BIGINT NOT NULL,
    subdomain VARCHAR(255) NOT NULL,
    access_token TEXT NOT NULL,
    refresh_token TEXT NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(company_id, account_id)
);

CREATE INDEX idx_amocrm_tokens_company_id ON amocrm_tokens(company_id);
CREATE INDEX idx_amocrm_tokens_expires_at ON amocrm_tokens(expires_at);