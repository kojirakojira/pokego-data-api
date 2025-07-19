/**
 * 原作ぼうぎょ、とくぼう、すばやさからGOの防御力を算出する。
 *
 * v_defense ぼうぎょ
 * v_sp_defense: とくぼう
 * v_speed: すばやさ
 */
CREATE OR REPLACE FUNCTION calc_go_base_defense(
  v_defense integer, 
  v_sp_defense integer, 
  v_speed integer)
RETURNS double precision AS $$
  DECLARE
    tmp_higher integer;
    tmp_lower integer;
    tmp_scaled_defense double precision;
  BEGIN
    IF v_defense > v_sp_defense THEN
      SELECT v_defense INTO tmp_higher;
      SELECT v_sp_defense INTO tmp_lower;
    ELSE
      SELECT v_sp_defense INTO tmp_higher;
      SELECT v_defense INTO tmp_lower;
    END IF;
    
    -- 2 × (0.625 × [高い方] + 0.375 × [低い方])を四捨五入
    SELECT ROUND(2 * (0.625 * tmp_higher + 0.375 * tmp_lower)) INTO tmp_scaled_defense;
    
    RAISE DEBUG 'tmp_scaled_defense: %', tmp_scaled_defense;
    -- すばやさ補正値を掛ける
    RETURN tmp_scaled_defense * calc_speed_mod(v_speed);
  END;
$$ LANGUAGE plpgsql
SET search_path TO public, pg_catalog, pg_temp;

/**
 * 原作ぼうぎょ、とくぼう、すばやさからGOのぼうぎょを算出する。
 * 強ポケ補正、種族値例外を考慮する。
 *
 * v_pokedex_id 図鑑ID
 * v_defense: 原作こうげき
 * v_sp_defense: 原作とくこう
 * v_speed: 原作すばやさ
 * v_correct_flg: 強ポケ補正フラグ
 */
CREATE OR REPLACE FUNCTION calc_go_defense(
  v_pokedex_id bpchar, 
  v_defense integer,
  v_sp_defense integer,
  v_speed integer,
  v_correct_flg boolean)
RETURNS INTEGER AS $$
  DECLARE
    ex_defense integer := null;
    tmp_defense double precision;
    ts_flg boolean; -- 強ポケ補正対象か否かを格納
    ts_correct_value CONSTANT double precision := 0.91; -- 普通のポケモンの場合の強ポケ補正値
    ts_correct_value_mega CONSTANT double precision := 0.97; -- メガシンカの場合の強ポケ補正値
  BEGIN
    -- 種族値例外の考慮。固定値が存在するポケモンの場合、固定値を返却する。
    SELECT re.defense INTO ex_defense
      FROM race_exceptions re
      WHERE re.pokedex_id = v_pokedex_id
      AND re.defense IS NOT NULL;

    IF ex_defense IS NOT NULL THEN
      RETURN ex_defense;
    END IF;

    -- 基礎となるGOのぼうぎょを算出
    SELECT calc_go_base_defense(v_defense, v_sp_defense, v_speed) INTO tmp_defense;

    IF v_correct_flg THEN
      -- 強ポケ補正フラグがtrueの場合、強ポケ補正対象か否かを取得する。
      SELECT count(*) > 0 INTO ts_flg
        FROM too_strong ts
        WHERE ts.pokedex_id = v_pokedex_id;

      IF ts_flg THEN
        -- 強ポケ補正対象の場合
        IF is_mega(v_pokedex_id) THEN
          -- メガシンカ
          SELECT tmp_defense * ts_correct_value_mega INTO tmp_defense;
        ELSE
          -- 普通のポケモン
          SELECT tmp_defense * ts_correct_value INTO tmp_defense;
        END IF;
      END IF;

    END IF;

    RAISE DEBUG 'tmp_defense: %', tmp_defense;

    -- 四捨五入
    RETURN round(tmp_defense::NUMERIC);

  END;
$$ LANGUAGE plpgsql
SET search_path TO public, pg_catalog, pg_temp;
