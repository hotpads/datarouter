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
package io.datarouter.util.incrementor;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.tuple.Range;

public class LocalDateIncrementor extends BaseRangeIncrementor<LocalDate>{

	private LocalDateIncrementor(Range<LocalDate> range){
		super(range);
	}

	public static LocalDateIncrementor fromInclusive(LocalDate start){
		return new LocalDateIncrementor(new Range<>(start, true, null, true));
	}

	@Override
	protected LocalDate increment(LocalDate item){
		return item.plusDays(1);
	}

	public static class LocalDateIncrementorTests{

		@Test
		public void test(){
			List<LocalDate> actual = LocalDateIncrementor.fromInclusive(LocalDate.of(2009, 3, 31))
					.step(2)
					.limit(2)
					.list();
			List<LocalDate> expected = Arrays.asList(
					LocalDate.of(2009, 3, 31),
					LocalDate.of(2009, 4, 2));
			Assert.assertEquals(actual, expected);
		}

	}

}