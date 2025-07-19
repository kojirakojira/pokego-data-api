/**
 * 強ポケ補正対象のポケモンを保持する。
 * 
 * PL50.5でCPが4000を超えるポケモンが強ポケ補正の対象である。（なんか怪しいけどそうみたい。）
 * メガシンカの場合は、メガシンカ前のポケモンで判定する。
 */
CREATE MATERIALIZED VIEW too_strong AS
  select
    p.pokedex_id
      from pokedex p
      inner join cp_multiplier cm
        on not is_mega(p.pokedex_id)
        and cm.pl = '50.5'
      where calc_cp(
        calc_go_hp(p.pokedex_id, p.hp, false),
          calc_go_attack(p.pokedex_id, p.attack, p.special_attack, p.speed, false),
          calc_go_defense(p.pokedex_id, p.defense, p.special_defense, p.speed, false),
          multiplier) > 4000
union
select
  p.pokedex_id
    from pokedex p
    inner join pokedex pbf
      on is_mega(p.pokedex_id)
      and get_pid_bf_mega(p.pokedex_id) = pbf.pokedex_id
    inner join cp_multiplier cm
      on cm.pl = '50.5'
    where 
      calc_cp(
        calc_go_hp(pbf.pokedex_id, pbf.hp, false),
        calc_go_attack(pbf.pokedex_id, pbf.attack, pbf.special_attack, pbf.speed, false),
        calc_go_defense(pbf.pokedex_id, pbf.defense, pbf.special_defense, pbf.speed, false),
        multiplier) > 4000;