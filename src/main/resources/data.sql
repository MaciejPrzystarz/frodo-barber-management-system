-- admin1@demo.pl admin123 --
INSERT INTO users (full_name, phone_number, email, password, role)
VALUES ('Admin Maciej', '234567891', 'admin1@demo.pl','$2b$10$415NvDFT7GwNFEL7Xgqhm.UcWlSl5U0Hv..SqZ7BRkTUs77lw19YO', 'ADMIN');

-- barber1@demo.pl barber123 --
INSERT INTO users (full_name, phone_number, email, password, role)
VALUES  ('Barber Krzysztof', '123456789', 'barber1@demo.pl', '$2b$10$zHyrkS13NMJ.T818GhMgmu/M2TsDh9yMEU4ChU5MeEl5ltQaKZnJy', 'BARBER');

-- client1@demo.pl client123 --
INSERT INTO users (full_name, phone_number, email, password, role) VALUES
    ('Client First', '345678912', 'client1@demo.pl', '$2b$10$W0Au1b3AIBRDh7qydDusUuAlzUDCSYwWUlbsxrlOjV4zKy0ldIHUW', 'CLIENT');

-- client2@demo.pl client123 --
INSERT INTO users (full_name, phone_number, email, password, role) VALUES
    ('Client Second', '456789123', 'client2@demo.pl', '$2b$10$W0Au1b3AIBRDh7qydDusUuAlzUDCSYwWUlbsxrlOjV4zKy0ldIHUW', 'CLIENT');

-- client3@demo.pl client123 --
INSERT INTO users (full_name, phone_number, email, password, role) VALUES
    ('Client Third', '567891234', 'client3@demo.pl', '$2b$10$W0Au1b3AIBRDh7qydDusUuAlzUDCSYwWUlbsxrlOjV4zKy0ldIHUW', 'CLIENT');

