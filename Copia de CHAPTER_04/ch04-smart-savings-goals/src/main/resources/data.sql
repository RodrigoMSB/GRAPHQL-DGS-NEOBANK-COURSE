-- Datos de prueba para Smart Savings Goals

INSERT INTO savings_goals (user_id, name, description, target_amount, current_amount, category, status)
VALUES
(1, 'Emergency Fund', 'Build 6 months of living expenses', 15000.00, 5000.00, 'EMERGENCY_FUND', 'ACTIVE'),
(1, 'Dream Vacation to Japan', 'Save for 2-week trip to Tokyo', 5000.00, 1200.00, 'VACATION', 'ACTIVE'),
(1, 'New MacBook Pro', 'Upgrade development laptop', 3500.00, 3500.00, 'OTHER', 'COMPLETED'),

(2, 'Home Down Payment', 'Save 20% for house purchase', 80000.00, 25000.00, 'HOME_PURCHASE', 'ACTIVE'),
(2, 'Kids Education Fund', 'College savings for 2 children', 100000.00, 15000.00, 'EDUCATION', 'ACTIVE'),

(3, 'Retirement Planning', 'Supplemental retirement savings', 500000.00, 75000.00, 'RETIREMENT', 'ACTIVE'),
(3, 'Investment Portfolio', 'Build diversified portfolio', 50000.00, 12000.00, 'INVESTMENT', 'ACTIVE'),
(3, 'Cruise Trip', 'Mediterranean cruise', 8000.00, 2000.00, 'VACATION', 'PAUSED');
