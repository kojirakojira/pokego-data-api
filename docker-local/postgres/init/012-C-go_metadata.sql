DROP TABLE IF EXISTS go_metadata;

CREATE TABLE go_metadata (
  pokedex_id bpchar, -- 図鑑ID
  release_date date, -- リリース年月
  dynamax_impl_flg boolean NOT NULL, -- ダイマックス実装済みフラグ
  gigantamax_impl_flg boolean NOT null, -- キョダイマックス実装済みフラグ
  CONSTRAINT dynamax_pk PRIMARY KEY(pokedex_id)
);

COMMIT;