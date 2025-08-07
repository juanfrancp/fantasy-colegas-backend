-- Inserta el jugador "vacío" que se usa para las posiciones libres en el equipo.
-- Ningún usuario puede seleccionar este jugador. Solo se utiliza internamente.
INSERT INTO players (name, image, total_points, is_placeholder)
VALUES ('Jugador Vacío', 'https://example.com/placeholder-image.png', 0, TRUE);

-- Inserta una liga de ejemplo
INSERT INTO leagues (name, description, image, is_private, join_code, number_of_players, team_size)
VALUES ('Liga de los colegas', 'Una liga de prueba para los colegas', 'https://example.com/liga-col.png', TRUE, 'COLEGAS24', 3, 5);

-- Inserta usuarios de ejemplo con contraseñas codificadas
-- La contraseña para todos es 'password'
-- Nuevo hash BCrypt para 'password': $2a$10$f6bS6S99F.B5u4Lh4R9kSu9xL9bB.B/G/c5e/J.r/q8j9/a.o.A.o
INSERT INTO users (username, email, password)
VALUES ('adminUser', 'admin@example.com', '$2a$10$33.QLLtdvpQf8cyZrS6JKuwVvCKxSMV/Vg4nHXHxuwDVR8QdcMquu');
INSERT INTO users (username, email, password)
VALUES ('johnDoe', 'john@example.com', '$2a$10$33.QLLtdvpQf8cyZrS6JKuwVvCKxSMV/Vg4nHXHxuwDVR8QdcMquu');
INSERT INTO users (username, email, password)
VALUES ('janeDoe', 'jane@example.com', '$2a$10$33.QLLtdvpQf8cyZrS6JKuwVvCKxSMV/Vg4nHXHxuwDVR8QdcMquu');

-- Asigna roles a los usuarios en la liga
-- El adminUser tiene el rol de ADMIN, los otros de PARTICIPANT
INSERT INTO user_league_roles (user_id, league_id, role)
VALUES (1, 1, 'ADMIN');
INSERT INTO user_league_roles (user_id, league_id, role)
VALUES (2, 1, 'PARTICIPANT');
INSERT INTO user_league_roles (user_id, league_id, role)
VALUES (3, 1, 'PARTICIPANT');

-- Inserta jugadores en la liga
INSERT INTO players (name, image, total_points, league_id, is_placeholder)
VALUES ('Jugador A', 'https://example.com/player-a.png', 10, 1, FALSE);
INSERT INTO players (name, image, total_points, league_id, is_placeholder)
VALUES ('Jugador B', 'https://example.com/player-b.png', 15, 1, FALSE);
INSERT INTO players (name, image, total_points, league_id, is_placeholder)
VALUES ('Jugador C', 'https://example.com/player-c.png', 8, 1, FALSE);
INSERT INTO players (name, image, total_points, league_id, is_placeholder)
VALUES ('Jugador D', 'https://example.com/player-d.png', 20, 1, FALSE);
INSERT INTO players (name, image, total_points, league_id, is_placeholder)
VALUES ('Jugador E', 'https://example.com/player-e.png', 5, 1, FALSE);

-- Configura los equipos (rosters) iniciales de los usuarios.
-- El usuario 'johnDoe' (id=2) tiene un equipo con los jugadores A y B y 3 posiciones vacías.
INSERT INTO roster_players (user_id, league_id, player_id, role)
VALUES (2, 1, 2, 'CAMPO');
INSERT INTO roster_players (user_id, league_id, player_id, role)
VALUES (2, 1, 3, 'PORTERO');
INSERT INTO roster_players (user_id, league_id, player_id, role)
VALUES (2, 1, 1, 'CAMPO');
INSERT INTO roster_players (user_id, league_id, player_id, role)
VALUES (2, 1, 1, 'CAMPO');
INSERT INTO roster_players (user_id, league_id, player_id, role)
VALUES (2, 1, 1, 'CAMPO');

-- El usuario 'janeDoe' (id=3) tiene un equipo con los jugadores C y D y 3 posiciones vacías.
INSERT INTO roster_players (user_id, league_id, player_id, role)
VALUES (3, 1, 4, 'CAMPO');
INSERT INTO roster_players (user_id, league_id, player_id, role)
VALUES (3, 1, 5, 'PORTERO');
INSERT INTO roster_players (user_id, league_id, player_id, role)
VALUES (3, 1, 1, 'CAMPO');
INSERT INTO roster_players (user_id, league_id, player_id, role)
VALUES (3, 1, 1, 'CAMPO');
INSERT INTO roster_players (user_id, league_id, player_id, role)
VALUES (3, 1, 1, 'CAMPO');