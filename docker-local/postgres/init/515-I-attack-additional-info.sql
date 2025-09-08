DELETE FROM attack_additional_info;

INSERT INTO attack_additional_info (pokedex_id, move_id, annotation_type, text, create_date)
  VALUES
    ('0384N01', 'FLY2013', 'アイテム「いんせき」を使うことにより覚えることができる。', 'learning_pattern', NOW() AT TIME ZONE 'Asia/Tokyo') -- レックウザのガリョウテンセイ
	;

COMMIT;