/**
 * 強ポケ補正対象のポケモンを保持する。
 * 
 * PL40.0で個体値MAX。CPが4000を超えるポケモンが強ポケ補正の対象である。
 * メガシンカの場合は、メガシンカ前のポケモンで判定する。
 * ※実行前に040-C-dummy-common-function.sqlを実行すること。実行し終わったら、071-C-common-function.sqlを実行すること。
 */
CREATE MATERIALIZED VIEW too_strong AS
  select
    p.pokedex_id
      from pokedex p
      inner join cp_multiplier cm
        on not is_mega(p.pokedex_id)
        and cm.pl = '40.0'
      where calc_cp(
        calc_go_hp(p.pokedex_id, p.hp, false) + 15,
          calc_go_attack(p.pokedex_id, p.attack, p.special_attack, p.speed, false) + 15,
          calc_go_defense(p.pokedex_id, p.defense, p.special_defense, p.speed, false) + 15,
          multiplier) > 4000
union
select
  p.pokedex_id
    from pokedex p
    inner join pokedex pbf
      on is_mega(p.pokedex_id)
      and get_pid_bf_mega(p.pokedex_id) = pbf.pokedex_id
    inner join cp_multiplier cm
      on cm.pl = '40.0'
    where 
      calc_cp(
        calc_go_hp(pbf.pokedex_id, pbf.hp, false) + 15,
        calc_go_attack(pbf.pokedex_id, pbf.attack, pbf.special_attack, pbf.speed, false) + 15,
        calc_go_defense(pbf.pokedex_id, pbf.defense, pbf.special_defense, pbf.speed, false) + 15,
        multiplier) > 4000;