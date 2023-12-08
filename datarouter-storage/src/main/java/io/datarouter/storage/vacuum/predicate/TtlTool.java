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

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.function.Function;
import java.util.function.Predicate;

import io.datarouter.types.BaseMilliTime;
import io.datarouter.types.MilliTime;
import io.datarouter.types.Ulid;

/**
 * Set a cutoff as a Duration before current time.
 * Test whether each object is before the cutoff.
 */
public class TtlTool{

	public static <T> Predicate<T> isExpiredDate(
			Duration age,
			Function<T,Date> timeExtractor){
		return new DateVacuumPredicate<>(
				System.currentTimeMillis() - age.toMillis(),
				timeExtractor);
	}

	public static <T> Predicate<T> isExpiredEpochMilli(
			Duration age,
			Function<T,Long> timeExtractor){
		return new EpochMilliVacuumPredicate<>(
				System.currentTimeMillis() - age.toMillis(),
				timeExtractor);
	}

	public static <T> Predicate<T> isExpiredInstant(
			Duration age,
			Function<T,Instant> timeExtractor){
		return new InstantVacuumPredicate<>(
				Instant.now().minus(age),
				timeExtractor);
	}

	public static <T> Predicate<T> isExpiredMilliTime(
			Duration age,
			Function<T,BaseMilliTime<?>> timeExtractor){
		return new MilliTimeVacuumPredicate<>(
				MilliTime.now().minus(age),
				timeExtractor);
	}

	public static <T> Predicate<T> isExpiredUlid(
			Duration age,
			Function<T,Ulid> timeExtractor){
		return new UlidVacuumPredicate<>(
				System.currentTimeMillis() - age.toMillis(),
				timeExtractor);
	}


}
