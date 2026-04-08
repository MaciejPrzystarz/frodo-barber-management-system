-- admin1@demo.pl admin123 --
INSERT INTO users (full_name, phone_number, email, password, role)
VALUES ('Admin Maciej', '234567891', 'admin1@demo.pl', '$2b$10$415NvDFT7GwNFEL7Xgqhm.UcWlSl5U0Hv..SqZ7BRkTUs77lw19YO','ADMIN');

-- barber1@demo.pl barber123 --
INSERT INTO users (full_name, phone_number, email, password, role)
VALUES ('FRODO Krzysztof', '123456789', 'barber1@demo.pl',
        '$2b$10$zHyrkS13NMJ.T818GhMgmu/M2TsDh9yMEU4ChU5MeEl5ltQaKZnJy', 'BARBER');

-- client1/2/3/.../10@demo.pl client123 --
INSERT INTO users (full_name, phone_number, email, password, role)
VALUES ('Client One', '111111111', 'client1@demo.pl', '$2b$10$W0Au1b3AIBRDh7qydDusUuAlzUDCSYwWUlbsxrlOjV4zKy0ldIHUW','CLIENT'),
       ('Client Two', '222222222', 'client2@demo.pl', '$2b$10$W0Au1b3AIBRDh7qydDusUuAlzUDCSYwWUlbsxrlOjV4zKy0ldIHUW','CLIENT'),
       ('Client Three', '333333333', 'client3@demo.pl', '$2b$10$W0Au1b3AIBRDh7qydDusUuAlzUDCSYwWUlbsxrlOjV4zKy0ldIHUW','CLIENT'),
       ('Client Four', '444444444', 'client4@demo.pl', '$2b$10$W0Au1b3AIBRDh7qydDusUuAlzUDCSYwWUlbsxrlOjV4zKy0ldIHUW','CLIENT'),
       ('Client Five', '555555555', 'client5@demo.pl', '$2b$10$W0Au1b3AIBRDh7qydDusUuAlzUDCSYwWUlbsxrlOjV4zKy0ldIHUW','CLIENT'),
       ('Client Six', '666666666', 'client6@demo.pl', '$2b$10$W0Au1b3AIBRDh7qydDusUuAlzUDCSYwWUlbsxrlOjV4zKy0ldIHUW','CLIENT'),
       ('Client Seven', '777777777', 'client7@demo.pl', '$2b$10$W0Au1b3AIBRDh7qydDusUuAlzUDCSYwWUlbsxrlOjV4zKy0ldIHUW','CLIENT'),
       ('Client Eight', '888888888', 'client8@demo.pl', '$2b$10$W0Au1b3AIBRDh7qydDusUuAlzUDCSYwWUlbsxrlOjV4zKy0ldIHUW','CLIENT'),
       ('Client Nine', '999999999', 'client9@demo.pl', '$2b$10$W0Au1b3AIBRDh7qydDusUuAlzUDCSYwWUlbsxrlOjV4zKy0ldIHUW','CLIENT'),
       ('Client Ten', '101010101', 'client10@demo.pl', '$2b$10$W0Au1b3AIBRDh7qydDusUuAlzUDCSYwWUlbsxrlOjV4zKy0ldIHUW','CLIENT');

INSERT INTO customers (full_name, phone_number)
VALUES ('Jan Kowalski', '123456789'),
       ('Frodo', '456789123'),
       ('Ireneusz', '789123456');

INSERT INTO services (name, price, duration_minutes)
VALUES ('strzyżenie maszynką', 60, 40),
       ('strzyżenie nożyczkami + maszynka', 60, 60),
       ('strzyżenie włosów shaverem', 30, 20),
       ('strzyżenie brody', 50, 30),
       ('combo - (maszynką + broda)', 100, 70),
       ('combo v1 - (nożyczkami + broda)', 100, 90),
       ('combo v2 - (włosy shaverem + strzyżenie broda)', 75, 60);

INSERT INTO appointments (client_id, barber_id, start_time, end_time, service_id, status)
VALUES (3, 2, '2026-04-05T14:00:00', '2026-04-05T14:40:00', 1, 'BOOKED'),
       (3, 2, '2026-04-16T17:20:00', '2026-04-16T18:00:00', 1, 'BOOKED'),
       (3, 2, '2026-05-05T12:00:00', '2026-05-05T12:40:00', 1, 'BOOKED'),
       (4, 2, '2026-04-05T15:30:00', '2026-04-05T16:30:00', 2, 'BOOKED'),
       (5, 2, '2026-04-15T12:00:00', '2026-04-15T12:20:00', 3, 'BOOKED'),
       (6, 2, '2026-04-13T11:10:00', '2026-04-13T11:40:00', 4, 'BOOKED'),
       (7, 2, '2026-04-14T11:00:00', '2026-04-14T12:10:00', 5, 'BOOKED'),
       (8, 2, '2026-04-15T14:00:00', '2026-04-15T15:30:00', 6, 'BOOKED'),
       (9, 2, '2026-04-15T15:30:00', '2026-04-15T16:30:00', 7, 'BOOKED');

