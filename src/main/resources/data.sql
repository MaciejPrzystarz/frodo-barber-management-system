-- admin@demo.pl admin123 --
INSERT INTO users (id, full_name, phone_number, email, password, role)
VALUES (1, 'Admin Maciej', '234 567 891', 'admin@demo.pl',
        '$2b$10$415NvDFT7GwNFEL7Xgqhm.UcWlSl5U0Hv..SqZ7BRkTUs77lw19YO', 'ADMIN');

-- barber@demo.pl barber123 --
INSERT INTO users (id, full_name, phone_number, email, password, role)
VALUES (2, 'Barber Krzysztof', '123 456 789', 'barber@demo.pl', '$2b$10$zHyrkS13NMJ.T818GhMgmu/M2TsDh9yMEU4ChU5MeEl5ltQaKZnJy', 'BARBER');

-- client@demo.pl client123 --
INSERT INTO users (id, full_name, phone_number, email, password, role) VALUES
    (3, 'Client Kowalski', '345 678 912', 'client@demo.pl', '$2b$10$W0Au1b3AIBRDh7qydDusUuAlzUDCSYwWUlbsxrlOjV4zKy0ldIHUW', 'CLIENT');
