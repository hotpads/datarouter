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

	public InstantNanoToLongFieldCodec(){
		super(TypeToken.get(Instant.class),
				NullPassthroughCodec.of(
						InstantNanoToLongFieldCodec::toEpochNano,
						InstantNanoToLongFieldCodec::fromEpochNano),
				Instant::compareTo,
				Instant.EPOCH,
				null);
	}

	private static long toEpochNano(Instant instant){
		return ChronoUnit.NANOS.between(Instant.EPOCH, instant);
	}

	private static Instant fromEpochNano(Long epochNano){
		return Instant.EPOCH.plusNanos(epochNano);
	}

}
