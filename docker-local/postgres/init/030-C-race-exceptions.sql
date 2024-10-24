DROP TABLE IF EXISTS race_exceptions;

CREATE TABLE race_exceptions (
  pokedex_id bpchar,
  hp integer,
  attack integer,
  defense integer,
  not_exists_origin boolean,
  CONSTRAINT race_exceptions_pk PRIMARY KEY(pokedex_id)
);