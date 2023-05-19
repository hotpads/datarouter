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
package io.datarouter.model.field.codec;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.google.gson.reflect.TypeToken;

import io.datarouter.bytes.Codec.NullPassthroughCodec;

public class InstantNanoToLongFieldCodec extends FieldCodec<Instant,Long>{

	private static final long NANOS_PER_SECOND = 1000_000_000L;

	// 2262-04-11T23:47:16.854775807Z
	public static final Instant MAX_ENCODABLE_INSTANT = Instant.EPOCH.plusNanos(Long.MAX_VALUE);
	// 1677-09-21T00:12:44Z
	// Instant.EPOCH.plusNanos(Long.MIN_VALUE) will have values that will overflow the
	// Chrono unit operations. see Math.multiplyExact
	// For that reason we truncate at the second level
	public static final Instant MIN_ENCODABLE_INSTANT =
			Instant.EPOCH.plusSeconds(Long.MIN_VALUE / NANOS_PER_SECOND);

	public InstantNanoToLongFieldCodec(){
		super(TypeToken.get(Instant.class),
				NullPassthroughCodec.of(
						InstantNanoToLongFieldCodec::toEpochNano,
						InstantNanoToLongFieldCodec::fromEpochNano),
				Instant::compareTo,
				Instant.EPOCH,
				null);
	}

	protected static long toEpochNano(Instant instant){
		if(instant.isAfter(MAX_ENCODABLE_INSTANT)){
			throw new IllegalArgumentException("Instant is after MAX_ENCODABLE_INSTANT " + MAX_ENCODABLE_INSTANT);
		}
		if(instant.isBefore(MIN_ENCODABLE_INSTANT)){
			throw new IllegalArgumentException("Instant is before MIN_ENCODABLE_INSTANT " + MIN_ENCODABLE_INSTANT);
		}
		return ChronoUnit.NANOS.between(Instant.EPOCH, instant);
	}

	protected static Instant fromEpochNano(Long epochNano){
		return Instant.EPOCH.plusNanos(epochNano);
	}

}
