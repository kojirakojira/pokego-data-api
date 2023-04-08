package jp.brainjuice.pokego;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.service.utils.dto.type.TwoTypeKey;

public class Tests {

	@Test
	public void testCase001() throws Exception {

		TwoTypeKey ttKey1 = new TwoTypeKey(TypeEnum.water, null);
		TwoTypeKey ttKey2 = new TwoTypeKey(TypeEnum.water, TypeEnum.water);
		Set<TwoTypeKey> list = new HashSet<>();
		list.add(ttKey1);
		assertTrue(list.contains(ttKey2));
	}
}
