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
package io.datarouter.gson;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import io.datarouter.gson.serializer.DurationLegacySerializer;
import io.datarouter.gson.serializer.InstantLegacySerializer;
import io.datarouter.gson.serializer.LocalDateLegacySerializer;
import io.datarouter.gson.serializer.LocalDateTimeLegacySerializer;
import io.datarouter.gson.serializer.LocalTimeLegacySerializer;
import io.datarouter.gson.serializer.MilliTimeReversedTypeAdapter;
import io.datarouter.gson.serializer.MilliTimeTypeAdapter;
import io.datarouter.gson.serializer.ReverseUlidTypeAdapter;
import io.datarouter.gson.serializer.UlidSerializer;
import io.datarouter.gson.serializer.ZoneIdLegacySerializer;
import io.datarouter.gson.typeadapter.QuadTypeAdapter;
import io.datarouter.gson.typeadapterfactory.EnumTypeAdapterFactory;
import io.datarouter.gson.typeadapterfactory.EnumTypeAdapterFactory.AnonymousAllowUnregisteredEnumTypeAdapterFactory;
import io.datarouter.gson.typeadapterfactory.EnumTypeAdapterFactory.RejectAllEnumTypeAdapterFactory;
import io.datarouter.gson.typeadapterfactory.OptionalLegacyTypeAdapterFactory;
import io.datarouter.types.MilliTime;
import io.datarouter.types.MilliTimeReversed;
import io.datarouter.types.Quad;
import io.datarouter.types.ReverseUlid;
import io.datarouter.types.Ulid;

public class GsonTool{

	//TODO make private and use more specific static methods
	public static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(Instant.class, new InstantLegacySerializer())
			.registerTypeAdapter(Duration.class, new DurationLegacySerializer())
			.registerTypeAdapter(LocalDate.class, new LocalDateLegacySerializer())
			.registerTypeAdapter(LocalTime.class, new LocalTimeLegacySerializer())
			.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeLegacySerializer())
			.registerTypeAdapterFactory(new OptionalLegacyTypeAdapterFactory())
			.registerTypeHierarchyAdapter(ZoneId.class, new ZoneIdLegacySerializer())
			.registerTypeAdapter(Quad.class, new QuadTypeAdapter())
			.registerTypeAdapter(Ulid.class, new UlidSerializer())
			.registerTypeAdapter(ReverseUlid.class, new ReverseUlidTypeAdapter())
			.registerTypeAdapter(MilliTime.class, new MilliTimeTypeAdapter())
			.registerTypeAdapter(MilliTimeReversed.class, new MilliTimeReversedTypeAdapter())
			.create();

	public static GsonBuilder builder(EnumTypeAdapterFactory enumTypeAdapterFactory){
		return GSON.newBuilder()
				.registerTypeAdapterFactory(enumTypeAdapterFactory);
	}

	/*---------- without enums ------------*/

	private static final Gson GSON_WITHOUT_ENUMS = GSON.newBuilder()
			.registerTypeAdapterFactory(RejectAllEnumTypeAdapterFactory.INSTANCE)
			.create();

	public static Gson withoutEnums(){
		return GSON_WITHOUT_ENUMS;
	}

	/**
	 * This can be used if you don't expect to serialize any enums and would like an error if an enum is found
	 */
	public static GsonBuilder builderWithoutEnums(){
		return GSON_WITHOUT_ENUMS.newBuilder();
	}

	/*------------ allow unregistered enums ---------------*/

	private static final Gson GSON_ALLOW_UNREGISTERED_ENUMS = GSON.newBuilder()
			.registerTypeAdapterFactory(AnonymousAllowUnregisteredEnumTypeAdapterFactory.INSTANCE)
			.create();

	/**
	 * @deprecated This is for a quick check to see if any enums are being serialized. If so it should be replaced with
	 *             a dedicated subclass of EnumTypeAdapterFactory. If not it should be replaced with
	 *             GsonTool.withoutEnums().
	 */
	@Deprecated
	public static Gson withUnregisteredEnums(){
		return GSON_ALLOW_UNREGISTERED_ENUMS;
	}

	/*---------------- forLogs ----------------*/

	/**
	 * private: please use GsonTool.forLogs() or create a dedicated Gson instance with:
	 *   GsonTool.GSON.newBuilder()
	 *     .setPrettyPrinting()
	 *     .create();
	 */
	private static final Gson GSON_PRETTY_PRINT = GSON.newBuilder()
			.setPrettyPrinting()
			.create();

	/**
	 * Quick solution for pretty-printing a value that is not parsed by another system.  Lacks things like enum
	 * validation.
	 */
	public static final Gson forLogsPretty(){
		return GSON_PRETTY_PRINT;
	}

	/**
	 * Quick solution for printing a value that is not parsed by another system.  Lacks things like enum validation.
	 */
	public static final Gson forLogs(){
		return GSON;
	}

	/*----------- reserialize -------------*/

	public static String prettyPrint(String json){
		return GSON_PRETTY_PRINT.toJson(GSON.fromJson(json, JsonElement.class));
	}

}
