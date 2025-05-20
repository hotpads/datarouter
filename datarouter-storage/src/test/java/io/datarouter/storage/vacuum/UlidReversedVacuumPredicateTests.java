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

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.vacuum.predicate.UlidReversedVacuumPredicate;
import io.datarouter.types.Ulid;
import io.datarouter.types.UlidReversed;

public class UlidReversedVacuumPredicateTests{

	@Test
	public void testUlidReversedVacuumPredicate(){
		long now = System.currentTimeMillis();
		long cutoff = now - 1000 * 60 * 10; // 10 minutes ago

		// Create ULIDs with timestamps before and after the cutoff
		// 11 min ago
		UlidReversed ulidOld = UlidReversed.toUlidReversed(Ulid.createRandomUlidForTimestamp(cutoff - 1000 * 60));
		// 9 min ago
		UlidReversed ulidNew = UlidReversed.toUlidReversed(Ulid.createRandomUlidForTimestamp(cutoff + 1000 * 60));

		record TestItem(UlidReversed ulid){
		}

		List<TestItem> items = List.of(new TestItem(ulidOld), new TestItem(ulidNew));

		UlidReversedVacuumPredicate<TestItem> predicate = new UlidReversedVacuumPredicate<>(
				cutoff,
				TestItem::ulid);

		List<TestItem> retained = Scanner.of(items)
				.exclude(predicate)
				.list();
		// Only ulidNew should be retained (timestamp after cutoff)
		Assert.assertEquals(retained, List.of(new TestItem(ulidNew)));
	}

}
