INSERT INTO users (full_name, phone_number, email, password, role)
VALUES ('Admin Maciej', '+48234567891', 'admin1@demo.pl', '$2b$10$415NvDFT7GwNFEL7Xgqhm.UcWlSl5U0Hv..SqZ7BRkTUs77lw19YO', 'ADMIN')
    ON CONFLICT (email) DO NOTHING;

INSERT INTO users (full_name, phone_number, email, password, role)
VALUES ('FRODO Krzysztof', '+48123456789', 'barber1@demo.pl', '$2b$10$zHyrkS13NMJ.T818GhMgmu/M2TsDh9yMEU4ChU5MeEl5ltQaKZnJy', 'BARBER')
    ON CONFLICT (email) DO NOTHING;

INSERT INTO users (full_name, phone_number, email, password, role)
VALUES
    ('Client One', '+48111111111', 'client1@demo.pl', '$2b$10$W0Au1b3AIBRDh7qydDusUuAlzUDCSYwWUlbsxrlOjV4zKy0ldIHUW', 'CLIENT'),
    ('Client Two', '+48222222222', 'client2@demo.pl', '$2b$10$W0Au1b3AIBRDh7qydDusUuAlzUDCSYwWUlbsxrlOjV4zKy0ldIHUW', 'CLIENT'),
    ('Client Three', '+48333333333', 'client3@demo.pl', '$2b$10$W0Au1b3AIBRDh7qydDusUuAlzUDCSYwWUlbsxrlOjV4zKy0ldIHUW', 'CLIENT'),
    ('Client Four', '+48444444444', 'client4@demo.pl', '$2b$10$W0Au1b3AIBRDh7qydDusUuAlzUDCSYwWUlbsxrlOjV4zKy0ldIHUW', 'CLIENT'),
    ('Client Five', '+48555555555', 'client5@demo.pl', '$2b$10$W0Au1b3AIBRDh7qydDusUuAlzUDCSYwWUlbsxrlOjV4zKy0ldIHUW', 'CLIENT'),
    ('Client Six', '+48666666666', 'client6@demo.pl', '$2b$10$W0Au1b3AIBRDh7qydDusUuAlzUDCSYwWUlbsxrlOjV4zKy0ldIHUW', 'CLIENT'),
    ('Client Seven', '+48777777777', 'client7@demo.pl', '$2b$10$W0Au1b3AIBRDh7qydDusUuAlzUDCSYwWUlbsxrlOjV4zKy0ldIHUW', 'CLIENT'),
    ('Client Eight', '+48888888888', 'client8@demo.pl', '$2b$10$W0Au1b3AIBRDh7qydDusUuAlzUDCSYwWUlbsxrlOjV4zKy0ldIHUW', 'CLIENT'),
    ('Client Nine', '+48999999999', 'client9@demo.pl', '$2b$10$W0Au1b3AIBRDh7qydDusUuAlzUDCSYwWUlbsxrlOjV4zKy0ldIHUW', 'CLIENT'),
    ('Client Ten', '+48101010101', 'client10@demo.pl', '$2b$10$W0Au1b3AIBRDh7qydDusUuAlzUDCSYwWUlbsxrlOjV4zKy0ldIHUW', 'CLIENT')
    ON CONFLICT (email) DO NOTHING;

-- Seed usług musi byc idempotentny: ten skrypt wykonuje sie przy kazdym starcie aplikacji
-- (spring.sql.init.mode=always) wstawiam tylko te uslugi, ktorych nazwy jeszcze nie ma.
INSERT INTO services (name, price, duration_minutes)
SELECT v.name, v.price, v.duration_minutes
FROM (VALUES
    ('strzyżenie maszynką', 60, 40),
    ('strzyżenie nożyczki + maszynka', 60, 60),
    ('strzyżenie włosów shaverem', 30, 20),
    ('strzyżenie brody', 50, 30),
    ('combo - maszynką + broda', 100, 70),
    ('combo v1 - nożyczkami + broda', 100, 90),
    ('combo v2 - włosy shaverem + strzyżenie broda', 75, 60)
) AS v(name, price, duration_minutes)
WHERE NOT EXISTS (
    SELECT 1 FROM services s WHERE s.name = v.name
);