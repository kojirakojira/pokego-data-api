DELETE FROM attack_additional_info;

INSERT INTO attack_additional_info (pokedex_id, move_id, annotation_type, text, create_date)
  VALUES
    ('0384N01', 'FLY2013', 'learning_pattern', 'アイテム「いんせき」を使うことにより覚えることができる。', NOW() AT TIME ZONE 'Asia/Tokyo') -- レックウザのガリョウテンセイ
	;

COMMIT;