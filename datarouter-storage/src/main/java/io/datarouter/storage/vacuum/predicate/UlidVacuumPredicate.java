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

import io.datarouter.types.Ulid;

/**
 * Set a cutoff time which is fixed on creation.
 * Test whether each object is before the cutoff.
 */
public class UlidVacuumPredicate<T> implements Predicate<T>{

	private final long epochMilliCutoff;
	private final Function<T,Ulid> ulidExtractor;

	public UlidVacuumPredicate(long epochMilliCutoff, Function<T,Ulid> ulidExtractor){
		this.epochMilliCutoff = epochMilliCutoff;
		this.ulidExtractor = ulidExtractor;
	}

	public UlidVacuumPredicate(Ulid cutoff, Function<T,Ulid> ulidExtractor){
		this(cutoff.getTimestampMs(), ulidExtractor);
	}

	@Override
	public boolean test(T item){
		return ulidExtractor.apply(item).getTimestampMs() < epochMilliCutoff;
	}

}
