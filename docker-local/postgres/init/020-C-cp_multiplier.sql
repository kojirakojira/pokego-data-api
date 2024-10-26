DROP TABLE IF EXISTS cp_multiplier;

CREATE TABLE cp_multiplier (
  pl bpchar(4),
  multiplier double PRECISION NOT NULL,
  CONSTRAINT cp_multiplier_pk PRIMARY KEY(pl)
);