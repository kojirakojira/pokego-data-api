DELETE FROM race_exceptions;

INSERT INTO race_exceptions (pokedex_id, hp, attack, defense, not_exists_origin)
  VALUES
    ('0150N02', '214', '182', '278', true), -- アーマードミュウツー
    ('0292N01', 1, null, null, false) -- ヌケニン
;

COMMIT;