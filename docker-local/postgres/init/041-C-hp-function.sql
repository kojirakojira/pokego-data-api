/**
 * 原作HPからGOのHPを算出する。
 * 強ポケ補正、種族値例外を考慮する。
 *
 * v_pokedex_id 図鑑ID
 * v_hp: 原作HP
 * v_correct_flg: 強ポケ補正フラグ
 */
CREATE OR REPLACE FUNCTION calc_go_hp(
  v_pokedex_id bpchar,
  v_hp integer,
  v_correct_flg boolean)
RETURNS INTEGER AS $$
  DECLARE
    ex_hp integer := null;
    tmp_hp double precision;
    ts_flg boolean; -- 強ポケ補正対象か否かを格納
    ts_correct_value CONSTANT double precision := 0.91; -- 普通のポケモンの場合の強ポケ補正値
    ts_correct_value_mega CONSTANT double precision := 0.97; -- メガシンカの場合の強ポケ補正値
  BEGIN
    -- 種族値例外の考慮。固定値が存在するポケモンの場合、固定値を返却する。
    SELECT re.hp INTO ex_hp
      FROM race_exceptions re
      WHERE re.pokedex_id = v_pokedex_id
      AND re.hp IS NOT NULL;

    IF ex_hp IS NOT NULL THEN
      RETURN ex_hp;
    END IF;

    -- 1.75 * HP + 50
    SELECT 1.75 * v_hp + 50 INTO tmp_hp;

    IF v_correct_flg THEN
      -- 強ポケ補正フラグがtrueの場合、強ポケ補正対象か否かを取得する。
      SELECT count(*) > 0 INTO ts_flg
        FROM too_strong ts
        WHERE ts.pokedex_id = v_pokedex_id;

      IF ts_flg THEN
        -- 強ポケ補正対象の場合
        IF is_mega(v_pokedex_id) THEN
          -- メガシンカ
          SELECT tmp_hp * ts_correct_value_mega INTO tmp_hp;
        ELSE
          -- 普通のポケモン
          SELECT tmp_hp * ts_correct_value INTO tmp_hp;
        END IF;
		-- 強ポケ補正後は四捨五入
		SELECT round(tmp_hp) INTO tmp_hp;
      END IF;

    END IF;

    -- 小数点以下切り捨て(double precision -> integerのキャスト。FLOORは何故か誤差が出る。)
    RETURN FLOOR(tmp_hp);

  END;
$$ LANGUAGE plpgsql
SET search_path TO public, pg_catalog, pg_temp;


/**
 * すばやさ補正値を算出する。
 *
 * v_speed: すばやさ
 */
CREATE OR REPLACE FUNCTION calc_speed_mod(v_speed integer)
RETURNS DOUBLE PRECISION AS $$
  DECLARE
    attack_d double precision;
  BEGIN
    -- double型にキャストする。
    SELECT v_speed INTO attack_d;
    RETURN 1 + (attack_d - 75) / 500;
  END;
$$ LANGUAGE plpgsql
SET search_path TO public, pg_catalog, pg_temp;
