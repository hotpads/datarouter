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
package io.datarouter.storage.vacuum.predicate;

import java.util.function.Function;
import java.util.function.Predicate;

import io.datarouter.types.UlidReversed;

public class UlidReversedVacuumPredicate<T> implements Predicate<T>{

	private final long epochMilliCutoff;
	private final Function<T,UlidReversed> ulidExtractor;

	public UlidReversedVacuumPredicate(long epochMilliCutoff, Function<T,UlidReversed> ulidExtractor){
		this.epochMilliCutoff = epochMilliCutoff;
		this.ulidExtractor = ulidExtractor;
	}

	@Override
	public boolean test(T item){
		return UlidReversed.toUlid(ulidExtractor.apply(item)).getTimestampMs() < epochMilliCutoff;
	}
}
