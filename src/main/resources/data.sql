MERGE INTO genres (name) KEY (name) VALUES ('Комедия');
MERGE INTO genres (name) KEY (name) VALUES ('Драма');
MERGE INTO genres (name) KEY (name) VALUES ('Мультфильм');
MERGE INTO genres (name) KEY (name) VALUES ('Триллер');
MERGE INTO genres (name) KEY (name) VALUES ('Документальный');
MERGE INTO genres (name) KEY (name) VALUES ('Боевик');

MERGE INTO rates (code) KEY (code) VALUES ('G');
MERGE INTO rates (code) KEY (code) VALUES ('PG');
MERGE INTO rates (code) KEY (code) VALUES ('PG-13');
MERGE INTO rates (code) KEY (code) VALUES ('R');
MERGE INTO rates (code) KEY (code) VALUES ('NC-17');