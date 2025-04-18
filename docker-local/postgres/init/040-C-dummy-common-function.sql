-- ダミーのファンクション
-- is_megaとget_pid_bf_mega、too_strong、go_pokedexは循環参照しているため、一度ダミーのファンクションを作る必要がある。
/**
 * メガシンカかどうかを判定する。
 *
 * v_pokedex_id: 図鑑ID
 */
CREATE OR REPLACE FUNCTION is_mega(v_pokedex_id bpchar)
RETURNS boolean AS $$
  BEGIN
    RETURN SUBSTRING(v_pokedex_id, 5, 1) = 'M';
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
  BEGIN
    IF is_mega(v_pokedex_id) THEN
      RETURN SUBSTRING(v_pokedex_id, 1, 4) || 'N01';
    END IF;

    RETURN v_pokedex_id;
  END;
$$ LANGUAGE plpgsql
SET search_path TO public, pg_catalog, pg_temp;
