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
package io.datarouter.httpclient.json;

import java.time.Instant;
import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

public class IsoDateGsonAdapterTests{

	private static final IsoDateGsonAdapter SERIALIZER = new IsoDateGsonAdapter();
	private static final Date DATE = Date.from(Instant.ofEpochMilli(1234567890123L));
	private static final JsonElement JSON_ELEMENT = new JsonPrimitive("2009-02-13T23:31:30.123Z");

	@Test
	public void testSerialize(){
		Assert.assertEquals(SERIALIZER.serialize(DATE, Date.class, null), JSON_ELEMENT);
		Assert.assertEquals(SERIALIZER.serialize(null, Date.class, null), JsonNull.INSTANCE);
	}

	@Test
	public void testDeserialize(){
		Assert.assertEquals(SERIALIZER.deserialize(JSON_ELEMENT, Date.class, null), DATE);
	}

}
