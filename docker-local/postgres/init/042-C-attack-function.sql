
/**
 * 原作こうげき、とくこう、すばやさからGOのこうげきを算出する。
 *
 * v_attack こうげき
 * v_sp_attack: とくこう
 * v_speed: すばやさ
 */
CREATE OR REPLACE FUNCTION calc_go_base_attack(
  v_attack integer, 
  v_sp_attack integer, 
  v_speed integer)
RETURNS double precision AS $$
  DECLARE
    tmp_higher integer;
    tmp_lower integer;
    tmp_scaled_attack double precision;
  BEGIN
    IF v_attack > v_sp_attack THEN
      SELECT v_attack INTO tmp_higher;
      SELECT v_sp_attack INTO tmp_lower;
    ELSE
      SELECT v_sp_attack INTO tmp_higher;
      SELECT v_attack INTO tmp_lower;
    END IF;
    
    -- 2 × (0.825 × [高い方] + 0.125 × [低い方])を四捨五入
    SELECT ROUND(2 * (0.875 * tmp_higher + 0.125 * tmp_lower)) INTO tmp_scaled_attack;

    RAISE DEBUG 'tmp_scaled_attack: %', tmp_scaled_attack;
    -- すばやさ補正値を掛ける
    RETURN tmp_scaled_attack * calc_speed_mod(v_speed);
  END;
$$ LANGUAGE plpgsql
SET search_path TO public, pg_catalog, pg_temp;

/**
 * 原作こうげき、とくこう、すばやさからGOのこうげきを算出する。
 * 強ポケ補正、種族値例外を考慮する。
 *
 * v_pokedex_id 図鑑ID
 * v_attack: 原作こうげき
 * v_sp_attack: 原作とくこう
 * v_speed: 原作すばやさ
 * v_correct_flg: 強ポケ補正フラグ
 */
CREATE OR REPLACE FUNCTION calc_go_attack(
  v_pokedex_id bpchar, 
  v_attack integer,
  v_sp_attack integer,
  v_speed integer,
  v_correct_flg boolean)
RETURNS INTEGER AS $$
  DECLARE
    ex_attack integer := null;
    tmp_attack double precision;
    ts_flg boolean; -- 強ポケ補正対象か否かを格納
    ts_correct_value CONSTANT double precision := 0.91; -- 普通のポケモンの場合の強ポケ補正値
    ts_correct_value_mega CONSTANT double precision := 0.97; -- メガシンカの場合の強ポケ補正値
  BEGIN
    -- 種族値例外の考慮。固定値が存在するポケモンの場合、固定値を返却する。
    SELECT re.attack INTO ex_attack
      FROM race_exceptions re
      WHERE re.pokedex_id = v_pokedex_id
      AND re.attack IS NOT NULL;

    IF ex_attack IS NOT NULL THEN
      RETURN ex_attack;
    END IF;

    -- 基礎となるGOのこうげきを算出
    SELECT calc_go_base_attack(v_attack, v_sp_attack, v_speed) INTO tmp_attack;

    IF v_correct_flg THEN
      -- 強ポケ補正フラグがtrueの場合、強ポケ補正対象か否かを取得する。
      SELECT count(*) > 0 INTO ts_flg
        FROM too_strong ts
        WHERE ts.pokedex_id = v_pokedex_id;

      IF ts_flg THEN
        -- 強ポケ補正対象の場合
        IF is_mega(v_pokedex_id) THEN
          -- メガシンカ
          SELECT tmp_attack * ts_correct_value_mega INTO tmp_attack;
        ELSE
          -- 普通のポケモン
          SELECT tmp_attack * ts_correct_value INTO tmp_attack;
        END IF;
      END IF;

    END IF;

    RAISE DEBUG 'tmp_attack: %', tmp_attack;
    -- 四捨五入
    RETURN round(tmp_attack::NUMERIC);

  END;
$$ LANGUAGE plpgsql
SET search_path TO public, pg_catalog, pg_temp;
