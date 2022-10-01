create table if not exists pokedex (
 pokedex_id char(7),
 name varchar(20) not null,
 hp integer not null,
 attack integer not null,
 defense integer not null,
 special_attack integer not null,
 special_defense integer not null,
 speed integer not null,
 remarks varchar(256),
 constraint pokedex_pk primary key(pokedex_id)
);

create table if not exists go_pokedex (
 pokedex_id char(7),
 name varchar(20) not null,
 attack integer not null,
 defense integer not null,
 hp integer not null,
 image varchar(256),
 remarks varchar(32),
 constraint go_pokedex_pk primary key(pokedex_id)
);
