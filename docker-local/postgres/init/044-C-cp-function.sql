
/**
 * GOのHP、こうげき、ぼうぎょから"素のCP"を算出する。
 *
 * v_hp: hp
 * v_attack: こうげき
 * v_defense: ぼうぎょ
 */
CREATE OR REPLACE FUNCTION calc_plain_cp(
  v_hp integer,
  v_attack integer,
  v_defense integer)
RETURNS DOUBLE PRECISION AS $$
  BEGIN
    RETURN v_attack::double precision * (|/ v_defense::double precision ) * (|/ v_hp::double precision );
  END;
$$ LANGUAGE plpgsql
SET search_path TO public, pg_catalog, pg_temp;

/**
 * GOのHP、こうげき、ぼうぎょとCP MultiplierからCPを算出する。
 *
 * v_hp: hp
 * v_attack: こうげき
 * v_defense: ぼうぎょ
 * v_multiplier CP Multiplier
 */
CREATE OR REPLACE FUNCTION calc_cp(
  v_hp integer,
  v_attack integer,
  v_defense integer,
  v_multiplier double precision)
RETURNS DOUBLE PRECISION AS $$
  DECLARE
    tmp_cp integer;
  BEGIN
    SELECT FLOOR(calc_plain_cp(v_hp, v_attack, v_defense) * (v_multiplier ^ 2.0) / 10.0) INTO tmp_cp;

    IF tmp_cp < 10 THEN
      SELECT 10 INTO tmp_cp;
    END IF;

    RETURN tmp_cp;
  END;
$$ LANGUAGE plpgsql
SET search_path TO public, pg_catalog, pg_temp;
