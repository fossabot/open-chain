CREATE TABLE main_blocks (
  id        INTEGER PRIMARY KEY REFERENCES blocks,
  signature VARCHAR NOT NULL
);