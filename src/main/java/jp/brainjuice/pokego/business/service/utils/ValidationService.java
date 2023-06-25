package jp.brainjuice.pokego.business.service.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.service.utils.dto.ValidationItem;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.form.req.research.ResearchRequest;
import jp.brainjuice.pokego.web.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.form.res.research.ResearchResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ValidationService {

	private static final String VALIDATION_PACKAGE_NAME = "javax.validation.constraints";

	/**
	 * 現在実装中のチェックパターンです。列挙名はjavax.validationのアノテーション名と対応します。
	 * @author saibabanagchampa
	 *
	 */
	public enum CheckPattern {
		Null,
		NotNull,
		Min,
		Max,
	}

	/**
	 * パッケージ名:"javax.validation.constraints"のアノテーションが第1引数のRequest継承クラスのフィールドに付与されている場合、<br>
	 * 専用のチェックロジックを実行します。<br>
	 * ※この独自仕様は、javax.validationにおいて、メソッドの継承が許可されていないことに起因しています。<br>
	 * 詳細：java.validation.ConstraintDeclarationException: HV000151<br>
	 * <br>
	 * チェックエラーの場合、BadRequestExceptionをスローします。
	 *
	 * @param req
	 * @param res
	 * @return
	 * @throws Exception
	 */
	public void validation(ResearchRequest req) throws Exception {

		StringBuffer sb = new StringBuffer();
		ValidationItem validItem = new ValidationItem(); // インスタンスを使いまわす。
		// Requestの値をivのマップにセット
		for (Field field: req.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			for (Annotation anno: field.getAnnotations()) {
				Class<? extends Annotation> clazz = anno.annotationType();
				if (clazz.getPackageName().equals(VALIDATION_PACKAGE_NAME)) {

					// TODO: if文で連結させているが、この形式をとるかは要検討。
					if (clazz == NotNull.class) {

						execCheck(validItem.setAll(field.getName(), CheckPattern.NotNull, null, field.get(req)), sb);
					} else if (clazz == Null.class) {

						execCheck(validItem.setAll(field.getName(), CheckPattern.Null, null, field.get(req)), sb);
					} else if (clazz == Min.class) {
						Min min = (Min) anno;
						execCheck(validItem.setAll(field.getName(), CheckPattern.Min, min.value(), field.get(req)), sb);
					} else if (clazz == Max.class) {
						Max max = (Max) anno;
						execCheck(validItem.setAll(field.getName(), CheckPattern.Max, max.value(), field.get(req)), sb);
					}
				}

				if (0 < sb.length()) {
					// エラーメッセージが設定されている場合。
					throw new BadRequestException(sb.toString());
				}
			}
		}
	}

	public boolean exec(List<ValidationItem> checkList, ResearchResponse res) {

		String msg = null;
		try {
			msg = exec(checkList);
			res.setMessage(msg);
			res.setMsgLevel(MsgLevelEnum.error);
			res.setSuccess(msg.isEmpty());
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		return msg.isEmpty();
	}

	public String exec(List<ValidationItem> checkList) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		StringBuffer sb = new StringBuffer();
		for (ValidationItem checkItem: checkList) {
			execCheck(checkItem, sb);
		}
		return sb.toString();
	}

	/**
	 * チェック項目ごとのチェックを実行します。
	 *
	 * @param checkItem
	 * @param sb
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	private void execCheck(ValidationItem checkItem, StringBuffer sb) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		Method method = CheckModule.class.getMethod(checkItem.getCheckPattern().name(), Object.class, Object.class, String.class);
		String msg = (String) method.invoke(null, checkItem.getConstraint(), checkItem.getValue(), checkItem.getItemName());
		sb.append(msg);

	}

	public static class CheckModule {

		public static String Null(Object constraint, Object val, String itemName) {
			// 値が制限以上なら○
			return val == null ?
					"" : MessageFormat.format("「{0}」はNullでなければなりません。", itemName);
		}

		public static String NotNull(Object constraint, Object val, String itemName) {
			// 値が制限以上なら○
			return val != null ?
					"" : MessageFormat.format("「{0}」はNullを許容しません。", itemName);
		}

		public static String Min(Object constraint, Object val, String itemName) {
			// 値が制限以上なら○
			return ((Long) constraint).intValue() <= ((Integer) val).intValue() ?
					"" : MessageFormat.format("「{0}」は{1}以上でなければなりません。実際：{2}\n", itemName, constraint, val);
		}

		public static String Max(Object constraint, Object val, String itemName) {
			// 値が制限以下なら○
			return ((Integer) val).intValue() <= ((Long) constraint).intValue() ?
					"" : MessageFormat.format("「{0}」は{1}以下でなければなりません。実際：{2}\n", itemName, constraint, val);
		}
	}
}
