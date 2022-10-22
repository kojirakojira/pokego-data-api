package jp.brainjuice.pokego.business.service.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ibm.icu.text.MessageFormat;

import jp.brainjuice.pokego.business.service.utils.dto.CheckInfo;
import jp.brainjuice.pokego.web.form.res.Response;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InputCheckService {

	public enum CheckPattern {
		min,
		max,
	}

	public boolean exec(List<CheckInfo> checkList, Response res) {

		String msg = null;
		try {
			msg = exec(checkList);
			res.setMessage(msg);
			res.setSuccess(msg.isEmpty());
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		return msg.isEmpty();
	}

	public String exec(List<CheckInfo> checkList) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		StringBuffer sb = new StringBuffer();
		for (CheckInfo checkInfo: checkList) {
			Method method = CheckModule.class.getMethod(checkInfo.getCheckPattern().name(), Object.class, Object.class, String.class);
			String msg = (String) method.invoke(null, checkInfo.getConstraint(), checkInfo.getValue(), checkInfo.getItemName());
			sb.append(msg);
		}
		return sb.toString();
	}

	public static class CheckModule {

		public static String min(Object constraint, Object val, String itemName) {
			// 値が制限以上なら○
			return ((int) constraint <= (int) val) ?
					"" : MessageFormat.format("「{0}」は{1}以上でなければなりません。実際：{2}\n", itemName, constraint, val);
		}

		public static String max(Object constraint, Object val, String itemName) {
			// 値が制限以下なら○
			return ((int) val <= (int) constraint) ?
					"" : MessageFormat.format("「{0}」は{1}以下でなければなりません。実際：{2}\n", itemName, constraint, val);
		}
	}
}
