/*
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
package io.datarouter.util.duration;

import java.util.concurrent.TimeUnit;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DatarouterDurationTests{

	@Test
	public void parserAndtoStringTest(){
		DatarouterDuration duration = new DatarouterDuration(3, TimeUnit.DAYS);
		Assert.assertEquals(duration.toString(), "3d");

		duration = new DatarouterDuration("1d2h65m15s");
		Assert.assertEquals(duration.toString(), "1d3h5m15s");

		duration = new DatarouterDuration("2h1d65m15s");
		Assert.assertEquals(duration.toString(), "1d3h5m15s");

		duration = new DatarouterDuration("15s");
		Assert.assertEquals(duration.toSecond(), 15);
		Assert.assertEquals(duration.toString(), "15s");

		duration = new DatarouterDuration("100000d5s123ms");
		Assert.assertEquals(duration.toString(), "100000d5s123ms");

		duration = new DatarouterDuration("100000 d 5s 123 ms");
		Assert.assertEquals(duration.toString(), "100000d5s123ms");

		duration = new DatarouterDuration("48h");
		Assert.assertEquals(duration.toString(), "2d");

		duration = new DatarouterDuration("0");
		Assert.assertEquals(duration.toString(), "0ms");

		duration = new DatarouterDuration("max");
		Assert.assertEquals(duration.toString(TimeUnit.NANOSECONDS),
				DatarouterDuration.MAX_VALUE.toString(TimeUnit.NANOSECONDS));

		duration = new DatarouterDuration("1d1d");
		Assert.assertEquals(duration.toString(), "2d");

		duration = new DatarouterDuration("4ms1us");
		Assert.assertEquals(duration.toString(TimeUnit.MICROSECONDS), "4ms1us");

		duration = new DatarouterDuration(1234, TimeUnit.NANOSECONDS);
		Assert.assertEquals(duration.toString(TimeUnit.MICROSECONDS), "1us");
		Assert.assertEquals(duration.toString(TimeUnit.NANOSECONDS), "1us");
	}

	@Test
	public void testIsDuration(){
		Assert.assertTrue(DatarouterDuration.isDuration("3d"));
		Assert.assertTrue(DatarouterDuration.isDuration("1d2h65m15s"));
		Assert.assertTrue(DatarouterDuration.isDuration("2h1d65m15s"));
		Assert.assertTrue(DatarouterDuration.isDuration("15s"));
		Assert.assertTrue(DatarouterDuration.isDuration("100000d5s123ms"));
		Assert.assertTrue(DatarouterDuration.isDuration("100000 d 5s 123 ms"));
		Assert.assertTrue(DatarouterDuration.isDuration("48h"));
		Assert.assertTrue(DatarouterDuration.isDuration("0"));
		Assert.assertTrue(DatarouterDuration.isDuration("1d1d"));
		Assert.assertTrue(DatarouterDuration.isDuration("4ms1us"));
		Assert.assertTrue(DatarouterDuration.isDuration("MAX"));
		Assert.assertFalse(DatarouterDuration.isDuration("1banana"));
		Assert.assertFalse(DatarouterDuration.isDuration("1.5d7m"));
		Assert.assertFalse(DatarouterDuration.isDuration("1d7"));
		Assert.assertFalse(DatarouterDuration.isDuration("max5d"));
	}

	@Test
	public void testConvertion(){
		Assert.assertEquals(new DatarouterDuration(1, TimeUnit.SECONDS).toMillis(), 1000);
	}

	@Test
	public void testIsLongerThan(){
		Assert.assertTrue(new DatarouterDuration(2, TimeUnit.DAYS)
				.isLongerThan(new DatarouterDuration(1, TimeUnit.DAYS)));
		Assert.assertFalse(new DatarouterDuration(3, TimeUnit.DAYS)
				.isLongerThan(new DatarouterDuration(4, TimeUnit.DAYS)));
	}

}
