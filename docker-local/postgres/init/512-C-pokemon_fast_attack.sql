DROP TABLE IF EXISTS POKEMON_FAST_ATTACK;

CREATE TABLE POKEMON_FAST_ATTACK (
  pokedex_id bpchar,
  move_id bpchar,
  learning_pattern bpchar NOT NULL,
  CONSTRAINT pokemon_fast_attack_pk PRIMARY KEY(pokedex_id, move_id)
);