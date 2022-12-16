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

public class ReverseUlidTests{

	@Test
	public void reverseUlidWithSameTimestamp(){
		Ulid ulid = new Ulid();
		ReverseUlid reverseUlid1 = ReverseUlid.toReverseUlid(ulid);
		ReverseUlid reverseUlid2 = ReverseUlid.toReverseUlid(ulid);
		Assert.assertEquals(reverseUlid1, reverseUlid2);
		Ulid ulidFromReverseUlid = ReverseUlid.toUlid(reverseUlid2);
		Assert.assertEquals(ulid, ulidFromReverseUlid);
	}

	@Test
	public void reverseUlidWithStrings(){
		Ulid ulid = new Ulid("01GGDTZWX437DCE6587SFJ9C14");
		ReverseUlid reverseUlid1 = ReverseUlid.toReverseUlid(ulid);
		Assert.assertEquals(reverseUlid1.reverseValue(), "7YFFJ5032VWRJKHSTQR6GDPKYV");
		Assert.assertEquals(ReverseUlid.toUlid(reverseUlid1).value(), "01GGDTZWX437DCE6587SFJ9C14");

		ReverseUlid reverseUlid2 = ReverseUlid.toReverseUlid(ulid);
		Assert.assertEquals(reverseUlid2, reverseUlid1);
	}

	@Test
	public void testUlidConversion(){
		List<Ulid> expected = Stream.generate(Ulid::new)
				.limit(10_000)
				.sorted()
				.toList();
		List<Ulid> actual = expected
				.stream()
				.map(ReverseUlid::toReverseUlid)
				.sorted(Comparator.reverseOrder())
				.map(ReverseUlid::toUlid)
				.toList();
		Assert.assertEquals(actual, expected);
	}

}
