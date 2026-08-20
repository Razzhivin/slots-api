--liquibase formatted sql 
--changeset author:3 
CREATE TABLE bookings (id UUID PRIMARY KEY DEFAULT gen_random_uuid(), status VARCHAR(50) NOT NULL DEFAULT 'CONFIRMED', start_time TIMESTAMP WITH TIME ZONE NOT NULL, end_time TIMESTAMP WITH TIME ZONE NOT NULL, created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP); 
CREATE TABLE booking_resources (booking_id UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE, resource_id BIGINT NOT NULL REFERENCES resources(id) ON DELETE CASCADE, PRIMARY KEY (booking_id, resource_id)); 
CREATE INDEX idx_bookings_time ON bookings(start_time, end_time) WHERE status = 'CONFIRMED'; 
