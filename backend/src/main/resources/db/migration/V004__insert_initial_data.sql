-- Insert Admin User
INSERT INTO users (id, username, email, password, role, phone_number, is_active)
VALUES ('11111111-1111-1111-1111-111111111111', 'admin', 'admin@footballapp.com', '$2a$12$Fhf2sYIJtuN9T81K8d/IxeIaL7zxt6Roy1gUiSZ1NioZl0TOdnmEi', 'ADMIN', '+905551112233', true);

-- Insert Pitches
INSERT INTO pitches (id, name, location, hourly_price, capacity, is_available)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Camp Nou', 'Barcelona', 150.00, 14, true);

INSERT INTO pitches (id, name, location, hourly_price, capacity, is_available)
VALUES ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Old Trafford', 'Manchester', 120.00, 10, true);

INSERT INTO pitches (id, name, location, hourly_price, capacity, is_available)
VALUES ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Local Pitch A', 'Istanbul', 50.00, 14, true);
