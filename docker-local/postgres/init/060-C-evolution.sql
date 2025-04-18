DROP TABLE IF EXISTS evolution;

-- ガーメイルのような、複数ポケモンから1体のポケモンに進化するパターンが存在するため、複合主キーにならざるを得ない。
CREATE TABLE evolution (
  pokedex_id bpchar,
  before_pokedex_id bpchar,
  candy integer NOT NULL,
  evolution_items varchar(30),
  buddy varchar(100),
  special_action varchar(100),
  lure_modules varchar(100),
  trade_evolution varchar(100),
  evol_annotations varchar(256),
  can_go_evol boolean NOT NULL,
  CONSTRAINT evolution_pk PRIMARY KEY(pokedex_id, before_pokedex_id)
);

CREATE INDEX IF NOT EXISTS evolution_before_pid_pk ON evolution (before_pokedex_id);

COMMIT;