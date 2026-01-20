USE chefpro;

INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');

-- CONTRASEÑA: admin123 (pero **cifrada** con BCrypt desde Spring Boot)
-- De momento la dejamos sin cifrar. Spring la actualizará.

INSERT INTO users (username, email, password)
VALUES ('admin', 'admin@chefpro.com', 'admin123');

INSERT INTO user_roles (user_id, role_id)
VALUES (1, 2);
