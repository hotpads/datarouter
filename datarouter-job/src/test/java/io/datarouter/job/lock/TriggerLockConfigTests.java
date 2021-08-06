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
package io.datarouter.job.lock;

import java.time.Duration;
import java.time.Instant;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TriggerLockConfigTests{

	@Test
	public void testSafePlus(){
		Assert.assertEquals(TriggerLockConfig.safePlus(TriggerLockConfig.MAX_DATE_INSTANT,
				TriggerLockConfig.MAX_DURATION), TriggerLockConfig.MAX_DATE_INSTANT);
		Assert.assertEquals(TriggerLockConfig.safePlus(TriggerLockConfig.MAX_DATE_INSTANT.minusSeconds(5),
				TriggerLockConfig.MAX_DURATION), TriggerLockConfig.MAX_DATE_INSTANT);
		Assert.assertEquals(TriggerLockConfig.safePlus(TriggerLockConfig.MAX_DATE_INSTANT,
				TriggerLockConfig.MAX_DURATION.minusSeconds(5)), TriggerLockConfig.MAX_DATE_INSTANT);

		Assert.assertEquals(TriggerLockConfig.safePlus(Instant.now(), TriggerLockConfig.MAX_DURATION),
				TriggerLockConfig.MAX_DATE_INSTANT);
		Assert.assertEquals(TriggerLockConfig.safePlus(Instant.MAX, Duration.ofMillis(0)),
				TriggerLockConfig.MAX_DATE_INSTANT);

		Assert.assertEquals(TriggerLockConfig.safePlus(Instant.MAX, TriggerLockConfig.MAX_DURATION),
				TriggerLockConfig.MAX_DATE_INSTANT);
		Assert.assertEquals(TriggerLockConfig.safePlus(Instant.MAX.minusSeconds(5), TriggerLockConfig.MAX_DURATION),
				TriggerLockConfig.MAX_DATE_INSTANT);
		Assert.assertEquals(TriggerLockConfig.safePlus(Instant.MAX, TriggerLockConfig.MAX_DURATION.minusSeconds(5)),
				TriggerLockConfig.MAX_DATE_INSTANT);
	}

}
