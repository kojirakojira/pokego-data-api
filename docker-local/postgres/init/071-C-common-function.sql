
/**
 * メガシンカかどうかを判定する。
 *
 * v_pokedex_id: 図鑑ID
 */
CREATE OR REPLACE FUNCTION is_mega(v_pokedex_id bpchar)
RETURNS boolean AS $$
  DECLARE
    tmp_pre_mega_pid bpchar;
  BEGIN
	SELECT gp.pre_mega_pokedex_id INTO tmp_pre_mega_pid
	  FROM go_pokedex gp
      WHERE pokedex_id = v_pokedex_id;
    RETURN tmp_pre_mega_pid IS NOT NULL;
  END;
$$ LANGUAGE plpgsql
SET search_path TO public, pg_catalog, pg_temp;

/**
 * メガシンカ前のポケモンのpokedex_idを取得する。
 * メガシンカでない場合は、そのまま返却する
 *
 * v_pokedex_id: 図鑑ID
 */
CREATE OR REPLACE FUNCTION get_pid_bf_mega(v_pokedex_id bpchar)
RETURNS bpchar AS $$
  DECLARE
    tmp_pre_mega_pid bpchar;
  BEGIN
	SELECT gp.pre_mega_pokedex_id INTO tmp_pre_mega_pid
	  FROM go_pokedex gp
      WHERE pokedex_id = v_pokedex_id;
    IF tmp_pre_mega_pid IS NOT NULL THEN
      RETURN tmp_pre_mega_pid;
    END IF;

    RETURN v_pokedex_id;
  END;
$$ LANGUAGE plpgsql
SET search_path TO public, pg_catalog, pg_temp;

COMMIT;
