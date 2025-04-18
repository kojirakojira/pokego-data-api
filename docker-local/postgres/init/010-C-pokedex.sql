DROP TABLE IF EXISTS pokedex;

CREATE TABLE pokedex (
  pokedex_id bpchar,
  name varchar(20) not null,
  hp integer not null,
  attack integer not null,
  defense integer not null,
  special_attack integer not null,
  special_defense integer not null,
  speed integer not null,
  remarks varchar(256) not null,
  type1 varchar(10) not null,
  type2 varchar(10),
  gen varchar(16) not null,
  image1 varchar(256),
  image2 varchar(256),
  impl_flg boolean not null,
  pre_mega_pokedex_id bpchar,
  CONSTRAINT pokedex_pk PRIMARY KEY(pokedex_id)
);