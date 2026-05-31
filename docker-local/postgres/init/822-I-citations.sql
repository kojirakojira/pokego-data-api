DELETE FROM citations;

INSERT INTO citations 
  VALUES 
    ('abundance', 1, 'pokemon.com', 'ポケモン図鑑({0})', 'https://zukan.pokemon.co.jp/detail/{1}'),
    ('abundance', 2, 'bulbapedia', 'List of Pokémon by availability in Pokémon GO', 'https://bulbapedia.bulbagarden.net/wiki/List_of_Pok%C3%A9mon_by_availability_(GO)'),
    ('abundance', 3, 'ポケモンWiki', 'Combat Power', 'https://wiki.pokemonwiki.com/wiki/Combat_Power'),
    ('abundance', 4, 'ポケモンWiki', 'ダメージ (GO)', 'https://wiki.pokemonwiki.com/wiki/%E3%83%80%E3%83%A1%E3%83%BC%E3%82%B8_(GO)');

COMMIT;
