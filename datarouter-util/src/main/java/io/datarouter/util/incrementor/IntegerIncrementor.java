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

import io.datarouter.util.tuple.Range;

public class IntegerIncrementor extends BaseRangeIncrementor<Integer>{

	private IntegerIncrementor(Range<Integer> range){
		super(range);
	}

	public static IntegerIncrementor fromInclusive(int startInclusive){
		return new IntegerIncrementor(new Range<>(startInclusive, true, null, true));
	}

	public static IntegerIncrementor toExclusive(int endExclusive){
		return new IntegerIncrementor(new Range<>(0, true, endExclusive, false));
	}

	@Override
	protected Integer increment(Integer item){
		return item + 1;
	}

}
