package jp.brainjuice.pokego.business.service.search.moves;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.service.search.utils.dto.moves.MoveSearchResult;
import jp.brainjuice.pokego.dao.jpa.FastAttackRepository;
import jp.brainjuice.pokego.dao.jpa.dto.SimpMove;
import jp.brainjuice.pokego.utils.BjUtils;
import jp.brainjuice.pokego.web.search.form.res.MsgLevelEnum;

@Service
public class MoveSearchService {

	private FastAttackRepository fastAttackRepository;

//	private ChargedAttackRepository chargedAttackRepository;

	private static final String MSG_RESULTS = "{0}件の技がヒットしました！";

	private static final String MSG_MAYBE = "なんだかよく分からなかったのでいい感じに検索しました！";

	private static final String MSG_NO_RESULTS = "該当する技がありませんでした。";

	private static final String MSG_NO_ENTERED = "入力してください。";

	public MoveSearchService(FastAttackRepository fastAttackRepository) {
		this.fastAttackRepository = fastAttackRepository;
	}

	public MoveSearchResult search(String words) {

		MoveSearchResult result = new MoveSearchResult();

		if (StringUtils.isEmpty(words)) {
			result.setMessage(MSG_NO_ENTERED);
			result.setMsgLevel(MsgLevelEnum.error);
			return result;
		}

		// 半角・全角スペースが存在する場合は分割してAND演算で一致する技名を検索する
		String[] splittedWords = words.replaceAll("　", " ").split(" ");
		List<SimpMove> simpMoveList = fastAttackRepository.findSimpMoveByNameLikeIn(splittedWords);
		result.setSearched(true);

		if (simpMoveList.isEmpty()) {
			// 検索結果なしだった場合
			if (20 < words.length()) {
				// なんか負荷がかかりそうだから文字数が多いときは検索させない。
				result.setMessage("20文字を超えた場合は、あいまい検索しません。");
				result.setMsgLevel(MsgLevelEnum.error);
				return result;
			}
			// すごく曖昧に検索する。
			simpMoveList = searchFuzzy(words);
			result.setMaybe(true);

			if (simpMoveList.isEmpty()) {
				// 曖昧検索後も0件の場合
				result.setMessage(MSG_NO_RESULTS);
				result.setMsgLevel(MsgLevelEnum.error);
				return result;
			}
		}

		// 空でない場合
		result.setHit(true);
		// メッセージの作成
		String msg = MessageFormat.format(MSG_RESULTS, simpMoveList.size());
		msg = result.isMaybe() ? msg + MSG_MAYBE : msg;
		result.setMessage(msg);

		if (simpMoveList.size() == 1) {
			// 1件のみヒットした場合
			result.setSimpMove(simpMoveList.get(0));
			result.setUnique(true);
		}

		result.setSimpMoveList(simpMoveList);

		return result;
	}

	/**
	 * 独自のロジックで曖昧に検索する。
	 *
	 * @param name
	 * @return
	 */
	private List<SimpMove> searchFuzzy(String name) {

		// 2文字単位で分割する。
		String[] nameArr = toFuzzyNameList(name).stream()
				.map(BjUtils::wrapWithPercent)
				.toArray(String[]::new);
		// 検索
		return fastAttackRepository.findSimpMoveByNameLikeIn(nameArr);

	}

	/**
	 * 文字を2文字ずつに区切ったリストを返却する。
	 * あいうえお → [あい, いう, うえ, えお]
	 *
	 * @param name
	 * @return
	 */
	private List<String> toFuzzyNameList(String name) {

		if (name.length() < 2) {
			return List.of(name);
		}

		char[] nameChars = name.toCharArray();

		List<String> list = new ArrayList<String>();
		for (int i = 0; i < nameChars.length; i++) {
			if (nameChars.length - i < 2) break;

			list.add(String.valueOf(nameChars[i]) + String.valueOf(nameChars[i + 1]));
		}
		return list;
	}

}
