-- Inserta el jugador "vacío" que se usa para las posiciones libres en el equipo.
-- Ningún usuario puede seleccionar este jugador. Solo se utiliza internamente.
INSERT INTO players (name, image, total_points, is_placeholder)
VALUES ('Jugador Vacío', 'https://example.com/placeholder-image.png', 0, TRUE);

-- #####################################################################################
-- # LIGAS
-- #####################################################################################

-- Inserta la primera liga (privada)
INSERT INTO leagues (name, description, image, is_private, join_code, number_of_players, team_size)
VALUES ('Liga de los Colegas', 'La liga privada de siempre', 'https://example.com/liga-col.png', TRUE, 'COLE24', 4, 5);

-- Inserta la segunda liga (pública)
INSERT INTO leagues (name, description, image, is_private, join_code, number_of_players, team_size)
VALUES ('Liga Pública de Verano', 'Abierta para todos', 'https://example.com/liga-publica.png', FALSE, 'SUMMER', 2, 4);


-- #####################################################################################
-- # USUARIOS
-- #####################################################################################

-- Inserta usuarios de ejemplo con contraseñas codificadas (la contraseña para todos es 'password')
INSERT INTO users (username, email, password)
VALUES ('adminuser', 'admin@example.com', '$2a$10$33.QLLtdvpQf8cyZrS6JKuwVvCKxSMV/Vg4nHXHxuwDVR8QdcMquu'),
       ('johndoe', 'john@example.com', '$2a$10$33.QLLtdvpQf8cyZrS6JKuwVvCKxSMV/Vg4nHXHxuwDVR8QdcMquu'),
       ('janedoe', 'jane@example.com', '$2a$10$33.QLLtdvpQf8cyZrS6JKuwVvCKxSMV/Vg4nHXHxuwDVR8QdcMquu'),
       ('peterpan', 'peter@example.com', '$2a$10$33.QLLtdvpQf8cyZrS6JKuwVvCKxSMV/Vg4nHXHxuwDVR8QdcMquu'),
       ('lisasimpson', 'lisa@example.com', '$2a$10$33.QLLtdvpQf8cyZrS6JKuwVvCKxSMV/Vg4nHXHxuwDVR8QdcMquu');


-- #####################################################################################
-- # ROLES DE USUARIO EN LAS LIGAS
-- #####################################################################################

-- Asigna roles a los usuarios en la "Liga de los Colegas" (league_id = 1)
INSERT INTO user_league_roles (user_id, league_id, role) VALUES (1, 1, 'ADMIN');
INSERT INTO user_league_roles (user_id, league_id, role) VALUES (2, 1, 'PARTICIPANT');
INSERT INTO user_league_roles (user_id, league_id, role) VALUES (3, 1, 'PARTICIPANT');
INSERT INTO user_league_roles (user_id, league_id, role) VALUES (4, 1, 'PARTICIPANT');

-- Asigna roles a los usuarios en la "Liga Pública de Verano" (league_id = 2)
INSERT INTO user_league_roles (user_id, league_id, role) VALUES (1, 2, 'ADMIN');
INSERT INTO user_league_roles (user_id, league_id, role) VALUES (5, 2, 'PARTICIPANT');


-- #####################################################################################
-- # JUGADORES
-- #####################################################################################

-- Inserta jugadores en la "Liga de los Colegas" (league_id = 1)
INSERT INTO players (name, image, total_points, league_id, is_placeholder)
VALUES ('Portero Estrella', 'https://example.com/player-a.png', 25, 1, FALSE),
       ('Defensa Central', 'https://example.com/player-b.png', 15, 1, FALSE),
       ('Medio Creativo', 'https://example.com/player-c.png', 30, 1, FALSE),
       ('Delantero Killer', 'https://example.com/player-d.png', 40, 1, FALSE),
       ('Banquillo 1', 'https://example.com/player-e.png', 5, 1, FALSE),
       ('Banquillo 2', 'https://example.com/player-f.png', 2, 1, FALSE);

-- Inserta jugadores en la "Liga Pública de Verano" (league_id = 2)
INSERT INTO players (name, image, total_points, league_id, is_placeholder)
VALUES ('Guardameta Sol', 'https://example.com/player-g.png', 18, 2, FALSE),
       ('Atacante Veloz', 'https://example.com/player-h.png', 22, 2, FALSE),
       ('Mago del Balón', 'https://example.com/player-i.png', 28, 2, FALSE);


-- #####################################################################################
-- # PARTIDOS
-- #####################################################################################

-- Partidos para la "Liga de los Colegas" (league_id = 1)
INSERT INTO matches (league_id, name, description, match_date)
VALUES (1, 'Partido jornada 1', 'Inicio de la temporada', '2025-08-10'),
       (1, 'Partido jornada 2', 'El gran clásico', '2025-08-17'),
       (1, 'Partido jornada 3', 'Partido pendiente', '2025-08-24');


-- #####################################################################################
-- # ESTADÍSTICAS DE PARTIDOS JUGADOS (CORREGIDO)
-- #####################################################################################

-- Estadísticas para el Partido 1 (match_id = 1)
INSERT INTO player_match_stats (match_id, player_id, goles_marcados, fallos_claros_de_gol, asistencias, goles_encajados_como_portero, paradas_como_portero, cesiones_concedidas, faltas_cometidas, faltas_recibidas, penaltis_recibidos, penaltis_cometidos, pases_acertados, pases_fallados, robos_de_balon, tiros_completados, tiros_entre_los_tres_palos, tiempo_jugado, tarjetas_amarillas, tarjetas_rojas, total_field_points, total_goalkeeper_points)
VALUES (1, 2, 0, 0, 0, 1, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 90, 0, 0, 0, 0.5), -- Portero Estrella
       (1, 5, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 90, 0, 0, 13, 0);  -- Delantero Killer

-- Estadísticas para el Partido 2 (match_id = 2)
INSERT INTO player_match_stats (match_id, player_id, goles_marcados, fallos_claros_de_gol, asistencias, goles_encajados_como_portero, paradas_como_portero, cesiones_concedidas, faltas_cometidas, faltas_recibidas, penaltis_recibidos, penaltis_cometidos, pases_acertados, pases_fallados, robos_de_balon, tiros_completados, tiros_entre_los_tres_palos, tiempo_jugado, tarjetas_amarillas, tarjetas_rojas, total_field_points, total_goalkeeper_points)
VALUES (2, 4, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 90, 1, 0, 5, 0),   -- Medio Creativo
       (2, 3, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 10, 0, 0, 90, 0, 0, 4, 0);  -- Defensa Central


-- #####################################################################################
-- # ROSTERS (EQUIPOS DE USUARIOS) (CORREGIDO)
-- #####################################################################################

-- Roster para 'johnDoe' (user_id = 2) en la "Liga de los Colegas" (league_id = 1)
INSERT INTO roster_players (user_id, league_id, player_id, role)
VALUES (2, 1, 2, 'PORTERO'),
       (2, 1, 3, 'CAMPO'),
       (2, 1, 5, 'CAMPO'),
       (2, 1, 1, 'CAMPO'), -- Jugador Vacío
       (2, 1, 1, 'CAMPO'); -- Jugador Vacío

-- Roster para 'janeDoe' (user_id = 3) en la "Liga de los Colegas" (league_id = 1)
INSERT INTO roster_players (user_id, league_id, player_id, role)
VALUES (3, 1, 2, 'PORTERO'),
       (3, 1, 4, 'CAMPO'),
       (3, 1, 6, 'CAMPO'),
       (3, 1, 7, 'CAMPO'),
       (3, 1, 1, 'CAMPO'); -- Jugador Vacío

-- Roster para 'lisaSimpson' (user_id = 5) en la "Liga Pública de Verano" (league_id = 2)
INSERT INTO roster_players (user_id, league_id, player_id, role)
VALUES (5, 2, 8, 'PORTERO'), -- CORREGIDO: league_id era 1, ahora es 2
       (5, 2, 9, 'CAMPO'), -- CORREGIDO: league_id era 1, ahora es 2
       (5, 2, 10, 'CAMPO'),-- CORREGIDO: league_id era 1, ahora es 2
       (5, 2, 1, 'CAMPO'); -- CORREGIDO: league_id era 1, ahora es 2


-- #####################################################################################
-- # SOLICITUDES DE UNIÓN
-- #####################################################################################

-- 'lisaSimpson' (user_id = 5) solicita unirse a la "Liga de los Colegas" (league_id = 1)
INSERT INTO league_join_requests (user_id, league_id, request_date, status)
VALUES (5, 1, '2025-08-18T10:00:00', 'PENDING');
