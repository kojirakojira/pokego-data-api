/**
 * ポケモンGOのポケモンを管理する。
 */
CREATE MATERIALIZED VIEW go_pokedex AS
  SELECT DISTINCT
      p.pokedex_id,
      p.name,
      p.name_en,
      calc_go_attack(p.pokedex_id, p.attack, p.special_attack, p.speed, true) as attack,
      calc_go_defense(p.pokedex_id, p.defense, p.special_defense, p.speed, true) as defense,
      calc_go_hp(p.pokedex_id, p.hp, true) as hp,
      p.remarks,
      p.type1,
      p.type2,
      p.gen,
      p.image1,
      p.image2,
      substring(p.pokedex_id, 5, 1)::bpchar(1) AS region,
      ts.pokedex_id IS NOT NULL AS too_strong,
      (e.pokedex_id IS NULL AND NOT(is_mega(p.pokedex_id))) AS fin_evo,
      gm.release_date,
      gm.release_date IS NOT NULL AS impl_flg,
      gm.dynamax_impl_flg,
      gm.gigantamax_impl_flg,
      p.official_zukan_id,
      p.pre_mega_pokedex_id
    FROM pokedex p
    LEFT JOIN too_strong ts
    ON p.pokedex_id = ts.pokedex_id
    LEFT JOIN evolution e
    ON p.pokedex_id = e.before_pokedex_id
    left join go_metadata gm
    on p.pokedex_id = gm.pokedex_id;

/**
 * CREATE INDEX
 */
CREATE INDEX IF NOT EXISTS go_pokedex_pk ON go_pokedex (pokedex_id);

COMMIT;