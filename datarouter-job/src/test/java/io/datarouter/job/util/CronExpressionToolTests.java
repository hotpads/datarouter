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
package io.datarouter.job.util;

import java.time.Duration;

import org.apache.logging.log4j.core.util.CronExpression;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.ComparableTool;
import io.datarouter.util.time.ZoneIds;

public class CronExpressionToolTests{

	@Test
	public void testHasUnevenInterval(){
		Assert.assertFalse(CronExpressionTool.hasUnevenInterval(60, "7"));
		Assert.assertFalse(CronExpressionTool.hasUnevenInterval(60, "7/1"));
		Assert.assertFalse(CronExpressionTool.hasUnevenInterval(60, "7/10"));
		Assert.assertTrue(CronExpressionTool.hasUnevenInterval(60, "7/11"));
	}

	@Test
	public void testDurationBetweenNextTwoTriggersFast(){
		CronExpression cron = CronExpressionTool.parse("3/15 * * * * ?", ZoneIds.UTC);
		Duration duration = CronExpressionTool.durationBetweenNextTwoTriggers(cron);
		Assert.assertEquals(duration, Duration.ofSeconds(15));
	}

	@Test
	public void testDurationBetweenNextTwoTriggersSlow(){
		Duration duration = CronExpressionTool.durationBetweenNextTwoTriggers("43 17 5 1 * ?", ZoneIds.UTC);
		Assert.assertTrue(ComparableTool.gt(duration, Duration.ofDays(27)));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testBadCronExpression(){
		CronExpressionTool.parse("not a cron expression", ZoneIds.UTC);
	}

}
