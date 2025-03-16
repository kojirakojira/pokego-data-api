/**
 * CP Multiplierの原表から本表を生成するときに使用する関数
 */
CREATE OR REPLACE FUNCTION calc_cpm(
  v_pl NUMERIC,
  v_multiplier real,
  v_half_down_cpm real, -- xx.5の-0.5したPLのmultiplier
  v_half_up_cpm real) -- xx.5の+0.5したPLのmultiplier
RETURNS double precision AS $$
  DECLARE
    cpm_result double precision;
  BEGIN
    IF MOD(v_pl, 1) > 0 THEN
      -- xx.5の場合は算出が必要
      SELECT |/(((v_half_down_cpm::double precision ^ 2.0) + (v_half_up_cpm::double precision ^ 2.0)) / 2) INTO cpm_result;
    ELSE
      -- xx.0の場合は型を変換して終了
      SELECT v_multiplier::double precision INTO cpm_result;
    END IF;
    RETURN cpm_result;
  END;
$$ LANGUAGE plpgsql
SET search_path TO public, pg_catalog, pg_temp;

COMMIT;
