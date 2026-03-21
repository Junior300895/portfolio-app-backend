-- ================================================
-- Portfolio Photographe - Database Init Script
-- ================================================

CREATE DATABASE IF NOT EXISTS portfolio_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE portfolio_db;

-- Admin user (password: Admin@2025 - change in production!)
-- Password hash generated with BCrypt strength 12
INSERT IGNORE INTO admin (username, email, password_hash, created_at)
VALUES (
    'admin',
    'admin@portfolio.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCkMzTGnf9JK3OgWxK2Yp2.',
    NOW()
);

-- Sample events
INSERT IGNORE INTO event (id, title, description, event_date, location, category, featured, created_at) VALUES
(1, 'Mariage Dupont - Martin', 'Reportage photo complet du mariage de Sophie et Alexandre. Une journée inoubliable au Château de Vaux-le-Vicomte, avec une lumière dorée exceptionnelle en fin de journée.', '2024-06-15', 'Château de Vaux-le-Vicomte, Maincy', 'MARIAGE', true, NOW()),
(2, 'Conférence Tech Summit 2024', 'Couverture photographique de la conférence annuelle Tech Summit réunissant 500 professionnels du numérique. Portraits de speakers, ambiances, moments forts.', '2024-09-20', 'Palais des Congrès, Paris', 'CONFERENCE', true, NOW()),
(3, 'Anniversaire 50 ans - Famille Leroux', 'Fête d''anniversaire surprise organisée pour les 50 ans de Marie-Claire. Reportage en lumière naturelle et artificielle, 4 heures de festivités.', '2024-11-08', 'Villa privée, Neuilly-sur-Seine', 'ANNIVERSAIRE', false, NOW()),
(4, 'Concert Jazz en Plein Air', 'Festival de jazz d''été au bord de Seine. Captures en longue exposition et haute vitesse des musiciens en action. Ambiances nocturnes exceptionnelles.', '2024-07-22', 'Berges de la Seine, Paris', 'CONCERT', true, NOW()),
(5, 'Séminaire Entreprise BNP', 'Séminaire annuel de team building et de stratégie de BNP Paribas. Portraits corporate, ateliers, moments conviviaux.', '2024-10-12', 'Centre de Conférences, La Défense', 'SEMINAIRE', false, NOW());
