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
package io.datarouter.util.serialization;

import java.time.ZoneId;
import java.time.ZoneOffset;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.gson.Gson;

import io.datarouter.gson.serialization.GsonTool;

public class ZoneIdLegacyTypeAdapterTests{

	@Test(dataProvider = "makeZoneIdsToTest")
	public void testInteroperability(ZoneId zoneId, String expectedJson){
		Gson legacyAdapterGson = GsonTool.GSON;
		String legacyAdapterJson = legacyAdapterGson.toJson(zoneId);
		Assert.assertEquals(legacyAdapterJson, expectedJson);

		// Remove to get to Java 16
		Gson legacyReflectionGson = new Gson();
		String legacyReflectionJson = legacyReflectionGson.toJson(zoneId);
		Assert.assertEquals(legacyReflectionJson, legacyAdapterJson);
	}

	@DataProvider
	public Object[][] makeZoneIdsToTest(){
		return new Object[][]{
			{ZoneId.of("Europe/Paris"), "{\"id\":\"Europe/Paris\"}"},
			{ZoneOffset.ofTotalSeconds(3), "{\"totalSeconds\":3}"}};
	}

}
