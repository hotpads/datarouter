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
package io.datarouter.types;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.Test;

public class UlidReversedTests{

	@Test
	public void ulidReversedWithSameTimestamp(){
		Ulid ulid = new Ulid();
		UlidReversed ulidReversed1 = UlidReversed.toUlidReversed(ulid);
		UlidReversed ulidReversed2 = UlidReversed.toUlidReversed(ulid);
		Assert.assertEquals(ulidReversed1, ulidReversed2);
		Ulid ulidFromReverseUlid = UlidReversed.toUlid(ulidReversed2);
		Assert.assertEquals(ulid, ulidFromReverseUlid);
	}

	@Test
	public void ulidReversedWithStrings(){
		Ulid ulid = new Ulid("01GGDTZWX437DCE6587SFJ9C14");
		UlidReversed ulidReversed1 = UlidReversed.toUlidReversed(ulid);
		Assert.assertEquals(ulidReversed1.reverseValue(), "7YFFJ5032VWRJKHSTQR6GDPKYV");
		Assert.assertEquals(UlidReversed.toUlid(ulidReversed1).value(), "01GGDTZWX437DCE6587SFJ9C14");

		UlidReversed ulidReversed2 = UlidReversed.toUlidReversed(ulid);
		Assert.assertEquals(ulidReversed2, ulidReversed1);
	}

	@Test
	public void testUlidConversion(){
		List<Ulid> expected = Stream.generate(Ulid::new)
				.limit(10_000)
				.sorted()
				.toList();
		List<Ulid> actual = expected.stream()
				.map(UlidReversed::toUlidReversed)
				.sorted(Comparator.reverseOrder())
				.map(UlidReversed::toUlid)
				.toList();
		Assert.assertEquals(actual, expected);
	}

}
