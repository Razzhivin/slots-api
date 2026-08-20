--liquibase formatted sql 
--changeset author:2 
CREATE TABLE resource_availability_intervals (id BIGSERIAL PRIMARY KEY, resource_id BIGINT NOT NULL REFERENCES resources(id) ON DELETE CASCADE, day_of_week VARCHAR(15) NOT NULL, start_time TIME NOT NULL, end_time TIME NOT NULL); 
CREATE INDEX idx_resource_day ON resource_availability_intervals(resource_id, day_of_week); 
