-- Inserta el jugador "vacío" que se usa para las posiciones libres en el equipo.
INSERT INTO players (name, image, total_points, is_placeholder)
VALUES ('Jugador Vacío', NULL, 0, TRUE);

-- #####################################################################################
-- # USUARIOS (15 usuarios en total)
-- #####################################################################################
-- La contraseña para todos es 'password'
INSERT INTO users (username, email, password) VALUES
('admin', 'admin@example.com', '$2a$10$33.QLLtdvpQf8cyZrS6JKuwVvCKxSMV/Vg4nHXHxuwDVR8QdcMquu'),
('ana_trader', 'ana@example.com', '$2a$10$33.QLLtdvpQf8cyZrS6JKuwVvCKxSMV/Vg4nHXHxuwDVR8QdcMquu'),
('carlos_pro', 'carlos@example.com', '$2a$10$33.QLLtdvpQf8cyZrS6JKuwVvCKxSMV/Vg4nHXHxuwDVR8QdcMquu'),
('beatriz_rm', 'beatriz@example.com', '$2a$10$33.QLLtdvpQf8cyZrS6JKuwVvCKxSMV/Vg4nHXHxuwDVR8QdcMquu'),
('david_crack', 'david@example.com', '$2a$10$33.QLLtdvpQf8cyZrS6JKuwVvCKxSMV/Vg4nHXHxuwDVR8QdcMquu'),
('elena_goleadora', 'elena@example.com', '$2a$10$33.QLLtdvpQf8cyZrS6JKuwVvCKxSMV/Vg4nHXHxuwDVR8QdcMquu'),
('fernando_manager', 'fernando@example.com', '$2a$10$33.QLLtdvpQf8cyZrS6JKuwVvCKxSMV/Vg4nHXHxuwDVR8QdcMquu'),
('gloria_fan', 'gloria@example.com', '$2a$10$33.QLLtdvpQf8cyZrS6JKuwVvCKxSMV/Vg4nHXHxuwDVR8QdcMquu'),
('hugo_pichichi', 'hugo@example.com', '$2a$10$33.QLLtdvpQf8cyZrS6JKuwVvCKxSMV/Vg4nHXHxuwDVR8QdcMquu'),
('irene_defensa', 'irene@example.com', '$2a$10$33.QLLtdvpQf8cyZrS6JKuwVvCKxSMV/Vg4nHXHxuwDVR8QdcMquu'),
-- Usuarios sin liga para probar solicitudes y unirse
('laura_newbie', 'laura@example.com', '$2a$10$33.QLLtdvpQf8cyZrS6JKuwVvCKxSMV/Vg4nHXHxuwDVR8QdcMquu'),
('marcos_aspirante', 'marcos@example.com', '$2a$10$33.QLLtdvpQf8cyZrS6JKuwVvCKxSMV/Vg4nHXHxuwDVR8QdcMquu'),
('nadia_sin_equipo', 'nadia@example.com', '$2a$10$33.QLLtdvpQf8cyZrS6JKuwVvCKxSMV/Vg4nHXHxuwDVR8QdcMquu'),
('oscar_observador', 'oscar@example.com', '$2a$10$33.QLLtdvpQf8cyZrS6JKuwVvCKxSMV/Vg4nHXHxuwDVR8QdcMquu'),
('pepe_leyenda', 'pepe@example.com', '$2a$10$33.QLLtdvpQf8cyZrS6JKuwVvCKxSMV/Vg4nHXHxuwDVR8QdcMquu');


-- #####################################################################################
-- # LIGAS (5 ligas en total)
-- #####################################################################################
INSERT INTO leagues (name, description, image, is_private, join_code, number_of_players, team_size) VALUES
('Leyendas del Sofá', 'Liga privada para los de siempre.', NULL, TRUE, 'SOFA25', 4, 5),
('Fichajes de Verano FC', 'Liga pública para la pretemporada. ¡Únete!', NULL, FALSE, 'VERANO', 3, 4),
('Campeones del Barrio', 'La más competitiva. Privada y con 2 admins.', NULL, TRUE, 'BARRIO', 5, 6),
('Tercer Tiempo', 'Liga pública y relajada para después del trabajo.', NULL, FALSE, 'CERVEZA', 2, 3),
('The Fantasist League', 'Liga privada de prueba para nuevos fichajes.', NULL, TRUE, 'TEST25', 1, 4);


-- #####################################################################################
-- # ROLES DE USUARIO EN LAS LIGAS
-- #####################################################################################
-- Liga 1: Leyendas del Sofá
INSERT INTO user_league_roles (user_id, league_id, role) VALUES
(1, 1, 'ADMIN'), (2, 1, 'PARTICIPANT'), (3, 1, 'PARTICIPANT'), (4, 1, 'PARTICIPANT');
-- Liga 2: Fichajes de Verano FC
INSERT INTO user_league_roles (user_id, league_id, role) VALUES
(5, 2, 'ADMIN'), (6, 2, 'PARTICIPANT'), (7, 2, 'PARTICIPANT');
-- Liga 3: Campeones del Barrio (CON DOS ADMINS)
INSERT INTO user_league_roles (user_id, league_id, role) VALUES
(1, 3, 'ADMIN'), (8, 3, 'ADMIN'), (9, 3, 'PARTICIPANT'), (10, 3, 'PARTICIPANT'), (2, 3, 'PARTICIPANT');
-- Liga 4: Tercer Tiempo
INSERT INTO user_league_roles (user_id, league_id, role) VALUES
(4, 4, 'ADMIN'), (5, 4, 'PARTICIPANT');
-- Liga 5: The Fantasist League
INSERT INTO user_league_roles (user_id, league_id, role) VALUES
(15, 5, 'ADMIN');


-- #####################################################################################
-- # SOLICITUDES DE UNIÓN PENDIENTES
-- #####################################################################################
INSERT INTO league_join_requests (user_id, league_id, request_date, status) VALUES
(11, 1, '2025-08-20T10:00:00', 'PENDING'),
(12, 1, '2025-08-21T11:30:00', 'PENDING'),
(13, 3, '2025-08-22T09:00:00', 'PENDING'),
(14, 5, '2025-08-23T12:00:00', 'PENDING');


-- #####################################################################################
-- # JUGADORES (con IDs a partir de 2 para no chocar con el placeholder)
-- #####################################################################################
INSERT INTO players (name, image, total_points, league_id, is_placeholder) VALUES
-- Liga 1
('El Muro', NULL, 30, 1, FALSE), ('El Káiser', NULL, 22, 1, FALSE), ('El Cerebro', NULL, 45, 1, FALSE), ('El Matador', NULL, 55, 1, FALSE), ('La Bala', NULL, 18, 1, FALSE),
-- Liga 2
('Gato Volador', NULL, 25, 2, FALSE), ('Toro Bravo', NULL, 15, 2, FALSE), ('El Mago', NULL, 33, 2, FALSE), ('El Relámpago', NULL, 41, 2, FALSE),
-- Liga 3
('La Pantera', NULL, 28, 3, FALSE), ('El Mariscal', NULL, 20, 3, FALSE), ('El Arquitecto', NULL, 38, 3, FALSE), ('El Tanque', NULL, 48, 3, FALSE), ('El Puñal', NULL, 31, 3, FALSE), ('El Pibe', NULL, 29, 3, FALSE),
-- Liga 4
('El Gato', NULL, 19, 4, FALSE), ('La Roca', NULL, 12, 4, FALSE), ('El Poeta', NULL, 26, 4, FALSE);


-- #####################################################################################
-- # EQUIPOS DE USUARIOS (ROSTERS)
-- #####################################################################################
-- Liga 1
INSERT INTO roster_players (user_id, league_id, player_id, role) VALUES
(2, 1, 2, 'PORTERO'), (2, 1, 3, 'CAMPO'), (2, 1, 4, 'CAMPO'), (2, 1, 5, 'CAMPO'), (2, 1, 1, 'CAMPO'),
(3, 1, 2, 'PORTERO'), (3, 1, 6, 'CAMPO'), (3, 1, 4, 'CAMPO'), (3, 1, 1, 'CAMPO'), (3, 1, 1, 'CAMPO'),
(4, 1, 2, 'PORTERO'), (4, 1, 3, 'CAMPO'), (4, 1, 6, 'CAMPO'), (4, 1, 1, 'CAMPO'), (4, 1, 1, 'CAMPO');
-- Liga 2
INSERT INTO roster_players (user_id, league_id, player_id, role) VALUES
(6, 2, 7, 'PORTERO'), (6, 2, 8, 'CAMPO'), (6, 2, 9, 'CAMPO'), (6, 2, 10, 'CAMPO'),
(7, 2, 7, 'PORTERO'), (7, 2, 8, 'CAMPO'), (7, 2, 1, 'CAMPO'), (7, 2, 1, 'CAMPO');
-- Liga 3
INSERT INTO roster_players (user_id, league_id, player_id, role) VALUES
(9, 3, 11, 'PORTERO'), (9, 3, 12, 'CAMPO'), (9, 3, 13, 'CAMPO'), (9, 3, 14, 'CAMPO'), (9, 3, 15, 'CAMPO'), (9, 3, 16, 'CAMPO'),
(10, 3, 11, 'PORTERO'), (10, 3, 12, 'CAMPO'), (10, 3, 1, 'CAMPO'), (10, 3, 1, 'CAMPO'), (10, 3, 1, 'CAMPO'), (10, 3, 1, 'CAMPO');

-- #####################################################################################
-- # SECCIÓN DE PARTIDOS
-- #####################################################################################

-- 1. CREACIÓN DE EQUIPOS PARA PARTIDOS (MATCH TEAMS)
INSERT INTO match_teams (name) VALUES
('Titanes del Sofá'), ('Leyendas Unidas'), ('Dream Team Clásico'), ('Héroes del Mando');
INSERT INTO match_teams (name) VALUES
('Barrio All-Stars'), ('Furia Callejera'), ('Atlético Vecindario'), ('Real Callejón');

-- 2. ASIGNACIÓN DE JUGADORES A LOS EQUIPOS DE PARTIDO
INSERT INTO match_team_players (match_team_id, player_id) VALUES
(1, 2), (1, 4), -- Titanes
(2, 3), (2, 5); -- Leyendas
INSERT INTO match_team_players (match_team_id, player_id) VALUES
(3, 6), (3, 5), -- Dream Team
(4, 2), (4, 4); -- Héroes
INSERT INTO match_team_players (match_team_id, player_id) VALUES
(5, 11), (5, 13), -- All-Stars
(6, 12), (6, 14); -- Furia
INSERT INTO match_team_players (match_team_id, player_id) VALUES
(7, 15), (7, 16), -- Atlético
(8, 11), (8, 14); -- Real

-- 3. CREACIÓN DE PARTIDOS (MATCHES)
-- CORREGIDO: Añadida columna 'status' obligatoria ('FINISHED' para pasados, 'SCHEDULED' para futuros)
INSERT INTO matches (league_id, home_team_id, away_team_id, home_score, away_score, match_date, status) VALUES
(1, 1, 2, 3, 1, '2025-08-20T20:00:00', 'FINISHED'),
(1, 3, 4, NULL, NULL, '2025-09-10T20:00:00', 'SCHEDULED');

INSERT INTO matches (league_id, home_team_id, away_team_id, home_score, away_score, match_date, status) VALUES
(3, 5, 6, 2, 2, '2025-08-25T21:00:00', 'FINISHED'),
(3, 7, 8, NULL, NULL, '2025-09-15T21:00:00', 'SCHEDULED');

-- #####################################################################################
-- # ESTADÍSTICAS DE PARTIDOS (PLAYER_MATCH_STATS)
-- #####################################################################################
-- CORREGIDO: Columnas actualizadas para coincidir con la BDD actual (quitando tiempo_jugado, pases, etc. y añadiendo nuevas)
-- Nuevas columnas: porteria_imbatida, salvadas_de_gol, penaltis_parados
INSERT INTO player_match_stats (match_id, player_id, goles_marcados, fallos_claros_de_gol, asistencias, goles_encajados_como_portero, paradas_como_portero, faltas_cometidas, faltas_recibidas, penaltis_recibidos, penaltis_cometidos, tarjetas_amarillas, tarjetas_rojas, total_field_points, total_goalkeeper_points, porteria_imbatida, salvadas_de_gol, penaltis_parados) VALUES
-- Partido 1, Portero 'El Muro' (player_id=2)
(1, 2, 0, 0, 0, 1, 5, 0, 2, 0, 0, 0, 0, 0, 8, FALSE, 0, 0),
-- Partido 1, Campo 'El Cerebro' (player_id=4)
(1, 4, 1, 0, 1, 0, 0, 1, 3, 1, 0, 0, 0, 12, 0, FALSE, 0, 0),
-- Partido 1, Campo 'El Matador' (player_id=5)
(1, 5, 2, 1, 0, 0, 0, 2, 1, 0, 0, 1, 0, 10, 0, FALSE, 0, 0);