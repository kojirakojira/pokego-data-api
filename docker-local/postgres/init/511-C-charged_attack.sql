DROP TABLE IF EXISTS CHARGED_ATTACK;

CREATE TABLE CHARGED_ATTACK (
  move_id bpchar,
  name varchar(40) NOT NULL,
  unique_id bpchar NOT NULL,
  movement_no bpchar NOT NULL,
  type varchar(10) NOT NULL,
  gym_power double precision NOT NULL,
  gym_energy_incr_amount integer NOT NULL,
  dps double precision NOT NULL,
  energy_bar integer NOT NULL,
  damage_ms integer NOT NULL,
  total_ms integer NOT NULL,
  pvp_power double precision NOT NULL,
  pvp_energy_incr_amount integer NOT NULL,
  dpe double precision NOT NULL,
  own_attack_buff integer NOT NULL,
  own_defense_buff integer NOT NULL,
  opp_attack_buff integer NOT NULL,
  opp_defense_buff integer NOT NULL,
  activation_chance double precision NOT NULL,
  CONSTRAINT charged_attack_pk PRIMARY KEY(move_id)
);