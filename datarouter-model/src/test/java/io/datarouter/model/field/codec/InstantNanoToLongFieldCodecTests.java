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

import org.testng.Assert;
import org.testng.annotations.Test;

public class InstantNanoToLongFieldCodecTests{

	@Test
	public void testEncodeBigInstant(){
		Assert.assertThrows(IllegalArgumentException.class,
				() -> InstantNanoToLongFieldCodec.toEpochNano(Instant.MAX));
	}

	@Test
	public void testEncodeSmallInstant(){
		Assert.assertThrows(IllegalArgumentException.class,
				() -> InstantNanoToLongFieldCodec.toEpochNano(Instant.MIN));
	}

	@Test
	public void testAfterEpochRoundTrip(){
		Instant afterEpoch = Instant.EPOCH.plusSeconds(100);
		Long nanos = InstantNanoToLongFieldCodec.toEpochNano(afterEpoch);
		Assert.assertEquals(InstantNanoToLongFieldCodec.fromEpochNano(nanos), afterEpoch);

		nanos = InstantNanoToLongFieldCodec.toEpochNano(InstantNanoToLongFieldCodec.MAX_ENCODABLE_INSTANT);
		Assert.assertEquals(InstantNanoToLongFieldCodec.fromEpochNano(nanos),
				InstantNanoToLongFieldCodec.MAX_ENCODABLE_INSTANT);
	}

	@Test
	public void testBeforeEpochRoundTrip(){
		Instant beforeEpoch = Instant.EPOCH.minusSeconds(100);
		Long nanos = InstantNanoToLongFieldCodec.toEpochNano(beforeEpoch);
		Assert.assertEquals(InstantNanoToLongFieldCodec.fromEpochNano(nanos), beforeEpoch);

		nanos = InstantNanoToLongFieldCodec.toEpochNano(InstantNanoToLongFieldCodec.MIN_ENCODABLE_INSTANT);
		Assert.assertEquals(InstantNanoToLongFieldCodec.fromEpochNano(nanos),
				InstantNanoToLongFieldCodec.MIN_ENCODABLE_INSTANT);
	}

}
