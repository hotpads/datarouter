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
package io.datarouter.util.time;

import java.time.Duration;
import java.time.Instant;

import org.testng.Assert;
import org.testng.annotations.Test;

public class InstantToolTests{

	@Test
	public void testIsOlderThan(){
		Instant instant = Instant.now().minusSeconds(60);
		Assert.assertTrue(InstantTool.isOlderThan(instant, Duration.ofSeconds(5)));
		Assert.assertFalse(InstantTool.isOlderThan(instant, Duration.ofMinutes(5)));
	}

}
