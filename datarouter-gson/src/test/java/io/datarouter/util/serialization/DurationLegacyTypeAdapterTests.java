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

import java.time.Duration;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.gson.Gson;

import io.datarouter.gson.serialization.GsonTool;

public class DurationLegacyTypeAdapterTests{

	@Test
	public void testInteroperability(){
		Duration duration = Duration.ofSeconds(13, 739457283);
		Gson legacyAdapterGson = GsonTool.GSON;
		String legacyAdapterJson = legacyAdapterGson.toJson(duration);
		Assert.assertEquals(legacyAdapterGson.fromJson(legacyAdapterJson, Duration.class), duration);
	}

}
