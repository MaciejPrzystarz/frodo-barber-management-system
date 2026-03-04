-- admin1@demo.pl admin123 --
INSERT INTO users (full_name, phone_number, email, password, role)
VALUES ('Admin Maciej', '234567891', 'admin1@demo.pl', '$2b$10$415NvDFT7GwNFEL7Xgqhm.UcWlSl5U0Hv..SqZ7BRkTUs77lw19YO',
        'ADMIN');

-- barber1@demo.pl barber123 --
INSERT INTO users (full_name, phone_number, email, password, role)
VALUES ('FRODO Krzysztof', '123456789', 'barber1@demo.pl',
        '$2b$10$zHyrkS13NMJ.T818GhMgmu/M2TsDh9yMEU4ChU5MeEl5ltQaKZnJy', 'BARBER');

-- client1@demo.pl client123 --
INSERT INTO users (full_name, phone_number, email, password, role)
VALUES ('Client First', '345678912', 'client1@demo.pl', '$2b$10$W0Au1b3AIBRDh7qydDusUuAlzUDCSYwWUlbsxrlOjV4zKy0ldIHUW',
        'CLIENT');

-- client2@demo.pl client123 --
INSERT INTO users (full_name, phone_number, email, password, role)
VALUES ('Client Second', '456789123', 'client2@demo.pl', '$2b$10$W0Au1b3AIBRDh7qydDusUuAlzUDCSYwWUlbsxrlOjV4zKy0ldIHUW',
        'CLIENT');

-- client3@demo.pl client123 --
INSERT INTO users (full_name, phone_number, email, password, role)
VALUES ('Client Third', '567891234', 'client3@demo.pl', '$2b$10$W0Au1b3AIBRDh7qydDusUuAlzUDCSYwWUlbsxrlOjV4zKy0ldIHUW',
        'CLIENT');

INSERT INTO services (name, price, duration_minutes)
VALUES ('strzyżenie maszynką', 60, 40),
       ('strzyżenie nożyczkami', 60, 60),
       ('strzyżenie włosów shaverem', 30, 20),
       ('strzyżenie brody', 50, 30),
       ('combo - (maszynka + broda)', 100, 75),
       ('combo v1 - (nożyczkami + broda)', 100, 90),
       ('combo v2 - (włosy shaverem + strzyżenie broda)', 75, 60);

INSERT INTO appointments (client_id, barber_id, start_time, end_time, service_id, status)
VALUES (3, 2, '2026-04-05T15:00:00', '2026-04-05T15:40:00', 1, 'BOOKED'),
       (5, 2, '2026-04-05T15:45:00', '2026-04-05T17:15:00', 6, 'BOOKED'),
       (4, 2, '2026-04-05T12:00:00', '2026-04-05T13:00:00', 2, 'BOOKED'),
       (5, 2, '2026-04-05T11:10:00', '2026-04-05T11:50:00', 1, 'BOOKED'),
       (5, 2, '2026-04-06T14:00:00', '2026-04-06T15:00:00', 7,'BOOKED');
