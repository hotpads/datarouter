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
package io.datarouter.storage.vacuum;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.vacuum.predicate.TtlTool;

public class InstantVacuumPredicateTests{

	@Test
	public void test(){
		Duration ttl = Duration.ofMinutes(2).plusSeconds(30);
		Predicate<Integer> predicate = TtlTool.isExpiredInstant(
				ttl,
				minutesAgo -> Instant.now().minus(Duration.ofMinutes(minutesAgo)));
		List<Integer> retainedMinutesAgo = Scanner.of(1, 2, 3, 4)//minutes ago
				.exclude(predicate)//should exclude 3, 4
				.list();
		Assert.assertEquals(retainedMinutesAgo, List.of(1, 2));
	}

}
