/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.ratelimiter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.ratelimiter.CacheRateLimiterConfig.CacheRateLimiterConfigBuilder;
import io.datarouter.ratelimiter.storage.BaseTallyDao;

@Guice(moduleFactory = RateLimiterTestNgModuleFactory.class)
public class BaseMapRateLimiterIntegrationTests{

	@Inject
	private BaseTallyDao tallyDao;

	private Calendar getCal() throws ParseException{
		Calendar calendar = Calendar.getInstance();
		Date date = new SimpleDateFormat("MMM dd HH:mm yyyy").parse("march 4 16:30 2010");
		calendar.setTime(date);
		calendar.set(Calendar.SECOND, 49);
		calendar.set(Calendar.MILLISECOND, 32);
		return calendar;
	}

	@Test
	public void testGetTimeStr() throws ParseException{
		Assert.assertEquals(makeTestRateLimiter(TimeUnit.DAYS).getTimeStr(getCal()), "20100228");
		Assert.assertEquals(makeTestRateLimiter(TimeUnit.HOURS).getTimeStr(getCal()), "2010030412");
		Assert.assertEquals(makeTestRateLimiter(TimeUnit.MINUTES).getTimeStr(getCal()), "201003041630");
		Assert.assertEquals(makeTestRateLimiter(TimeUnit.SECONDS).getTimeStr(getCal()), "20100304163045");
		Assert.assertEquals(makeTestRateLimiter(TimeUnit.MILLISECONDS).getTimeStr(getCal()), "20100304163049030");
	}

	private BaseCacheRateLimiter makeTestRateLimiter(TimeUnit unit){
		var config = new CacheRateLimiterConfigBuilder("MapRateLimiterIntegrationTests")
				.setMaxAverageRequests(0L)
				.setMaxSpikeRequests(0L)
				.setNumIntervals(0)
				.setBucketTimeInterval(5, unit)
				.build();
		return new BaseTallyCacheRateLimiter(tallyDao, config);
	}

}
