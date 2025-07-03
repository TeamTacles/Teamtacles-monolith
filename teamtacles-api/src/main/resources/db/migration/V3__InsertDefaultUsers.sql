INSERT INTO users (user_name, email, password) VALUES ('Pedro Lauton', 'pedro@admin.com', '$2a$10$Dn9PZOlmr1F4PXHcFJYEXOdvU0X2kQ9qXxHnFS2K7l5hBgldO4SUO');
INSERT INTO users (user_name, email, password) VALUES ('Caio Dib Laronga', 'caio@admin.com', '$2a$10$Dn9PZOlmr1F4PXHcFJYEXOdvU0X2kQ9qXxHnFS2K7l5hBgldO4SUO');
INSERT INTO users (user_name, email, password) VALUES ('Gabriela Santana', 'gabriela@admin.com', '$2a$10$Dn9PZOlmr1F4PXHcFJYEXOdvU0X2kQ9qXxHnFS2K7l5hBgldO4SUO');
INSERT INTO users (user_name, email, password) VALUES ('admin', 'admin@example.com', '$2a$10$B00g27tXj4c00r3B1tY70OuO2j4.2n8C7.Rj9k9P9L9P9O9M9N9L9O');


INSERT INTO user_roles (user_id, role_id)
SELECT u.user_id, r.id
FROM users u, role r
WHERE u.user_name = 'Pedro Lauton' AND r.role_name = 'ADMIN';

INSERT INTO user_roles (user_id, role_id)
SELECT u.user_id, r.id
FROM users u, role r
WHERE u.user_name = 'Caio Dib Laronga' AND r.role_name = 'ADMIN';

INSERT INTO user_roles (user_id, role_id)
SELECT u.user_id, r.id
FROM users u, role r
WHERE u.user_name = 'Gabriela Santana' AND r.role_name = 'ADMIN';

INSERT INTO user_roles (user_id, role_id)
SELECT u.user_id, r.id
FROM users u, role r
WHERE u.user_name = 'admin' AND r.role_name = 'ADMIN';