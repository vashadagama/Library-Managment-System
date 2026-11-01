INSERT INTO roles (name) VALUES
    ('ADMIN'),
    ('LIBRARIAN'),
    ('READER')
ON CONFLICT (name) DO NOTHING;

INSERT INTO user_statuses (name) VALUES
    ('ACTIVE'),
    ('BLOCKED')
ON CONFLICT (name) DO NOTHING;

INSERT INTO item_statuses (name) VALUES
    ('AVAILABLE'),
    ('BORROWED'),
    ('UNDER_REPAIR'),
    ('LOST'),
    ('WITHDRAWN')
ON CONFLICT (name) DO NOTHING;

INSERT INTO loan_statuses (name) VALUES
    ('ACTIVE'),
    ('RETURNED'),
    ('RENEWED'),
    ('OVERDUE'),
    ('LOST')
ON CONFLICT (name) DO NOTHING;

INSERT INTO languages (name) VALUES
    ('Русский'),
    ('Английский'),
    ('Немецкий')
ON CONFLICT (name) DO NOTHING;

INSERT INTO genres (name) VALUES
    ('FICTION'),
    ('CHILDREN'),
    ('SCIENTIFIC'),
    ('POPULAR_SCIENTIFIC')
ON CONFLICT (name) DO NOTHING;

INSERT INTO notification_types (name) VALUES
    ('DUE_SOON'),
    ('OVERDUE'),
    ('RESERVATION_READY'),
    ('ACCOUNT_BLOCKED')
ON CONFLICT (name) DO NOTHING;

INSERT INTO audit_types (name) VALUES
    ('CREATE'),
    ('UPDATE'),
    ('DELETE'),
    ('LOGIN'),
    ('ISSUE'),
    ('RETURN'),
    ('RENEW')
ON CONFLICT (name) DO NOTHING;

INSERT INTO system_parameters (key, value, description) VALUES
    ('max_active_loans_default', '5', 'Лимит активных выдач по умолчанию'),
    ('loan_period_days', '14', 'Срок выдачи в днях'),
    ('fine_per_day', '10.00', 'Штраф за день просрочки')
ON CONFLICT (key) DO NOTHING;
