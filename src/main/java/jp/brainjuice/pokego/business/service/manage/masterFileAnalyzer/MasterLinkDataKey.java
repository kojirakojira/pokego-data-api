package jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer;

/**
 * master_link_data.ymlのキーになる値
 */
public enum MasterLinkDataKey {
	pokemon,
	moves,
	quick_moves,
	cinematic_moves,
	tune_moves,
	limited_time_learned_cinematic_moves,
	not_defined_cinematic_moves,
	movement_id,
	move_category,
	/** AからBにフォルムチェンジするときは、フォルムチェンジ扱いにはしない */
	a_to_b_form_change_not_treated
}
