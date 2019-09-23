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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import io.datarouter.util.tuple.Range;

public class LocalDateTimeIncrementor extends BaseRangeIncrementor<LocalDateTime>{

	private final ChronoUnit chronoUnit;

	private LocalDateTimeIncrementor(Range<LocalDateTime> range, ChronoUnit chronoUnit){
		super(range);
		this.chronoUnit = chronoUnit;
	}

	public static LocalDateTimeIncrementor fromInclusive(LocalDateTime start, ChronoUnit unit){
		return new LocalDateTimeIncrementor(new Range<>(start, true, null, true), unit);
	}

	@Override
	protected LocalDateTime increment(LocalDateTime item){
		return item.plus(1, chronoUnit);
	}

}
