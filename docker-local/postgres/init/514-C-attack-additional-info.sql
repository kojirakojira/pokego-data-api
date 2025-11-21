DROP TABLE IF EXISTS ATTACK_ADDITIONAL_INFO;

CREATE TABLE ATTACK_ADDITIONAL_INFO (
  pokedex_id bpchar,
  move_id bpchar,
  annotation_type bpchar,
  text varchar NOT NULL,
  create_date timestamp DEFAULT NOW(),
  CONSTRAINT attack_additional_info_pk PRIMARY KEY(pokedex_id, move_id, annotation_type)
);