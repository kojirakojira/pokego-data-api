package jp.brainjuice.pokego;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class Tests {

	@Test
	public void testCase001() throws Exception {

		String name = "あ";
		char[] nameChars = name.toCharArray();

		List<String> list = new ArrayList<String>();
		for (int i = 0; i < nameChars.length; i++) {

			if (nameChars.length - i < 2) {
				break;
			}

			list.add(String.valueOf(nameChars[i]) + String.valueOf(nameChars[i + 1]));

		}

		list.forEach(System.out::println);

	}
}
