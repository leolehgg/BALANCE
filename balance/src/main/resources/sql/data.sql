-- Inicializar roles
INSERT INTO roles (name) VALUES ('ROLE_USER') ON CONFLICT DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_ADMIN') ON CONFLICT DO NOTHING;

-- Inicializar categorías
INSERT INTO categories (name, description) VALUES ('Productivity', 'Work and productivity apps') ON CONFLICT DO NOTHING;
INSERT INTO categories (name, description) VALUES ('Social Media', 'Social networking apps') ON CONFLICT DO NOTHING;
INSERT INTO categories (name, description) VALUES ('Entertainment', 'Entertainment and media apps') ON CONFLICT DO NOTHING;
INSERT INTO categories (name, description) VALUES ('Games', 'Gaming apps') ON CONFLICT DO NOTHING;
INSERT INTO categories (name, description) VALUES ('Education', 'Learning and educational apps') ON CONFLICT DO NOTHING;
INSERT INTO categories (name, description) VALUES ('Utilities', 'Utility and tool apps') ON CONFLICT DO NOTHING;