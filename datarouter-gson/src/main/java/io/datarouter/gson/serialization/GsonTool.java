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
package io.datarouter.gson.serialization;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class GsonTool{

	public static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(Instant.class, new InstantLegacyTypeAdapter())
			.registerTypeAdapter(Duration.class, new DurationLegacyTypeAdapter())
			.registerTypeAdapter(LocalDate.class, new LocalDateLegacyTypeAdapter())
			.registerTypeAdapter(LocalTime.class, new LocalTimeLegacyTypeAdapter())
			.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeLegacyTypeAdapter())
			.registerTypeAdapterFactory(new OptionalLegacyTypeAdapterFactory())
			.registerTypeHierarchyAdapter(ZoneId.class, new ZoneIdLegacyTypeAdapter())
			.registerTypeAdapterFactory(new RecordTypeAdapterFactory())
			.create();

	public static final Gson GSON_PRETTY_PRINT = GSON.newBuilder()
			.setPrettyPrinting()
			.create();

	public static String prettyPrint(String json){
		return GSON_PRETTY_PRINT.toJson(GSON.fromJson(json, JsonElement.class));
	}

}
