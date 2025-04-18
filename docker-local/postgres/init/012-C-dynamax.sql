DROP TABLE IF EXISTS dynamax;

CREATE TABLE dynamax (
  pokedex_id bpchar, -- 図鑑ID
  dynamax_impl_flg boolean NOT NULL, -- ダイマックス実装済みフラグ
  gigantamax_impl_flg boolean NOT null, -- キョダイマックス実装済みフラグ
  CONSTRAINT dynamax_pk PRIMARY KEY(pokedex_id)
);