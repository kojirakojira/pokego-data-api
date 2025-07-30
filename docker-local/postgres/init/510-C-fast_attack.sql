DROP TABLE IF EXISTS FAST_ATTACK;

CREATE TABLE FAST_ATTACK (
  move_id bpchar,
  name varchar(40) NOT NULL,
  unique_id bpchar NOT NULL,
  movement_no bpchar NOT NULL,
  type varchar(10) NOT NULL,
  gym_power double precision NOT NULL,
  gym_energy_incr_amount integer NOT NULL,
  dps double precision NOT NULL,
  eps double precision NOT NULL,
  damage_ms integer NOT NULL,
  total_ms integer NOT NULL,
  pvp_power double precision NOT NULL,
  pvp_energy_incr_amount integer NOT NULL,
  turns integer NOT NULL,
  dpt double precision NOT NULL,
  ept double precision NOT NULL,
  CONSTRAINT fast_attack_pk PRIMARY KEY(move_id)
);