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
package io.datarouter.gson.serializer;

import java.time.LocalDateTime;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.gson.Gson;

import io.datarouter.gson.DatarouterGsons;

public class LocalDateTimeLegacySerializerTests{

	@Test
	public void testInteroperability(){
		LocalDateTime localDateTime = LocalDateTime.of(2022, 3, 24, 5, 21, 54, 654634554);
		Gson legacyAdapterGson = DatarouterGsons.withoutEnums();
		String legacyAdapterJson = legacyAdapterGson.toJson(localDateTime);
		Assert.assertEquals(legacyAdapterGson.fromJson(legacyAdapterJson, LocalDateTime.class), localDateTime);
	}

}
