CREATE SEQUENCE IF NOT EXISTS pokemon_view_seq;

CREATE TABLE IF NOT EXISTS pokemon_view (
  pokemon_view_id integer DEFAULT nextval('pokemon_view_seq') NOT NULL,
  pokedex_id bpchar(7) NOT NULL,
  ymd timestamp NOT NULL,
  view_count integer NOT NULL,
  PRIMARY KEY(pokemon_view_id)
);

CREATE INDEX IF NOT EXISTS pokemon_view_idx ON pokemon_view (pokedex_id, ymd);