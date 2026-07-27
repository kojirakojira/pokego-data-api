package jp.brainjuice.pokego.business.service.search.pinnacle;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jp.brainjuice.pokego.web.search.form.res.pinnacle.GymRaidPinnacleRankResponse;

@SpringBootTest
public class GymRaidPinnacleRankServiceTest {

	@Autowired
	private GymRaidPinnacleRankService gymRaidPinnacleRankService;

	@Test
	public void testCase() {
		GymRaidPinnacleRankResponse res = new GymRaidPinnacleRankResponse();
		gymRaidPinnacleRankService.exec(null, res);
	}
}
