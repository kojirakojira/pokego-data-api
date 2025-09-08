DROP TABLE IF EXISTS POKEMON_CHARGED_ATTACK;

CREATE TABLE POKEMON_CHARGED_ATTACK (
  pokedex_id bpchar,
  move_id bpchar,
  learning_pattern bpchar NOT NULL,
  CONSTRAINT pokemon_charged_attack_pk PRIMARY KEY(pokedex_id, move_id)
);