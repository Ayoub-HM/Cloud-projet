CREATE TABLE IF NOT EXISTS notes (
  id BIGSERIAL PRIMARY KEY,
  title   VARCHAR(200) NOT NULL,
  content VARCHAR(2000) NOT NULL
);

INSERT INTO notes (title, content)
VALUES ('hello', 'first note in DB')
ON CONFLICT DO NOTHING;
