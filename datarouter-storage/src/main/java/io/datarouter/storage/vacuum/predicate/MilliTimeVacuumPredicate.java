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

import io.datarouter.types.BaseMilliTime;

/**
 * Set a cutoff time which is fixed on creation.
 * Test whether each object is before the cutoff.
 */
public class MilliTimeVacuumPredicate<T> implements Predicate<T>{

	private final long epochMilliCutoff;
	private final Function<T,BaseMilliTime<?>> timeExtractor;

	public MilliTimeVacuumPredicate(long epochMilliCutoff, Function<T,BaseMilliTime<?>> timeExtractor){
		this.epochMilliCutoff = epochMilliCutoff;
		this.timeExtractor = timeExtractor;
	}

	public MilliTimeVacuumPredicate(BaseMilliTime<?> cutoff, Function<T,BaseMilliTime<?>> timeExtractor){
		this(cutoff.toEpochMilli(), timeExtractor);
	}

	@Override
	public boolean test(T item){
		return timeExtractor.apply(item).toEpochMilli() < epochMilliCutoff;
	}

}
