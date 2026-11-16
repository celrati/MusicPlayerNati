--liquibase formatted sql

--changeset achraf:001-insert-dummy-data

INSERT INTO songs (title, artist, genre, duration, file_path) VALUES
('Bohemian Rhapsody', 'Queen', 'ROCK', 354, '/music/queen-bohemian.mp3'),
('Stairway to Heaven', 'Led Zeppelin', 'ROCK', 482, '/music/led-zeppelin-stairway.mp3'),
('Back in Black', 'AC/DC', 'ROCK', 255, '/music/acdc-back-in-black.mp3'),
('Comfortably Numb', 'Pink Floyd', 'ROCK', 382, '/music/pink-floyd-comfortably-numb.mp3'),
('Paint It Black', 'The Rolling Stones', 'ROCK', 222, '/music/stones-paint-it-black.mp3'),
('Sweet Child O'' Mine', 'Guns N'' Roses', 'ROCK', 356, '/music/gnr-sweet-child.mp3'),
('Sultans of Swing', 'Dire Straits', 'ROCK', 348, '/music/dire-straits-sultans.mp3'),
('Hotel California', 'Eagles', 'ROCK', 391, '/music/eagles-hotel-california.mp3');

-- playlists
INSERT INTO playlists (name, description, created_at, updated_at) VALUES
('70s Classic Rock', 'The absolute best rock anthems from the 1970s', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Guitar Legends', 'Epic guitar solos and timeless riffs', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- joint table
INSERT INTO playlist_songs (playlist_id, song_id, song_order) VALUES
(1, 1, 0), -- Bohemian Rhapsody (Queen)
(1, 2, 1), -- Stairway to Heaven (Led Zeppelin)
(1, 4, 2), -- Comfortably Numb (Pink Floyd)
(1, 5, 3), -- Paint It Black (The Rolling Stones)
(1, 8, 4); -- Hotel California (Eagles)

-- joint table
INSERT INTO playlist_songs (playlist_id, song_id, song_order) VALUES
(2, 3, 0), -- Back in Black (AC/DC)
(2, 6, 1), -- Sweet Child O' Mine (Guns N' Roses)
(2, 7, 2); -- Sultans of Swing (Dire Straits)