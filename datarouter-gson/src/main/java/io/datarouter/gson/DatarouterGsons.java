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

import io.datarouter.gson.serializer.DurationLegacySerializer;
import io.datarouter.gson.serializer.InstantLegacySerializer;
import io.datarouter.gson.serializer.LocalDateLegacySerializer;
import io.datarouter.gson.serializer.LocalDateTimeLegacySerializer;
import io.datarouter.gson.serializer.LocalTimeLegacySerializer;
import io.datarouter.gson.serializer.MilliTimeReversedTypeAdapter;
import io.datarouter.gson.serializer.MilliTimeTypeAdapter;
import io.datarouter.gson.serializer.UlidReversedTypeAdapter;
import io.datarouter.gson.serializer.UlidSerializer;
import io.datarouter.gson.serializer.ZoneIdLegacySerializer;
import io.datarouter.gson.typeadapter.QuadTypeAdapter;
import io.datarouter.gson.typeadapterfactory.DateTypeAdapterFactory;
import io.datarouter.gson.typeadapterfactory.EnumTypeAdapterFactory;
import io.datarouter.gson.typeadapterfactory.EnumTypeAdapterFactory.AnonymousAllowUnregisteredEnumTypeAdapterFactory;
import io.datarouter.gson.typeadapterfactory.EnumTypeAdapterFactory.RejectAllEnumTypeAdapterFactory;
import io.datarouter.gson.typeadapterfactory.OptionalLegacyTypeAdapterFactory;
import io.datarouter.types.MilliTime;
import io.datarouter.types.MilliTimeReversed;
import io.datarouter.types.Quad;
import io.datarouter.types.Ulid;
import io.datarouter.types.UlidReversed;

public class DatarouterGsons{

	/**
	 * private: please obtain a Gson from a more specific method.
	 */
	private static final Gson DATAROUTER_ROOT_GSON = new GsonBuilder()
			.registerTypeAdapter(Instant.class, new InstantLegacySerializer())
			.registerTypeAdapter(Duration.class, new DurationLegacySerializer())
			.registerTypeAdapter(LocalDate.class, new LocalDateLegacySerializer())
			.registerTypeAdapter(LocalTime.class, new LocalTimeLegacySerializer())
			.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeLegacySerializer())
			.registerTypeAdapterFactory(new OptionalLegacyTypeAdapterFactory())
			.registerTypeHierarchyAdapter(ZoneId.class, new ZoneIdLegacySerializer())
			.registerTypeAdapter(Quad.class, new QuadTypeAdapter())
			.registerTypeAdapter(Ulid.class, new UlidSerializer())
			.registerTypeAdapter(UlidReversed.class, new UlidReversedTypeAdapter())
			.registerTypeAdapter(MilliTime.class, new MilliTimeTypeAdapter())
			.registerTypeAdapter(MilliTimeReversed.class, new MilliTimeReversedTypeAdapter())
			.registerTypeAdapterFactory(new DateTypeAdapterFactory())
			.create();

	/**
	 * private: please use forDisplayPretty() or forPrettyPrint().
	 */
	private static final Gson DATAROUTER_GSON_PRETTY_PRINT = DATAROUTER_ROOT_GSON.newBuilder()
			.setPrettyPrinting()
			.create();

	/**
	 * private: please use withoutEnums().
	 */
	private static final Gson DATAROUTER_GSON_WITHOUT_ENUMS = DATAROUTER_ROOT_GSON.newBuilder()
			.registerTypeAdapterFactory(RejectAllEnumTypeAdapterFactory.INSTANCE)
			.create();

	/**
	 * private: please use withUnregisteredEnums().
	 */
	private static final Gson DATAROUTER_GSON_ALLOW_UNREGISTERED_ENUMS = DATAROUTER_ROOT_GSON.newBuilder()
			.registerTypeAdapterFactory(AnonymousAllowUnregisteredEnumTypeAdapterFactory.INSTANCE)
			.create();

	/**
	 * Use sparingly.
	 */
	public static Gson rootDatarouterGsonInstance(){
		return DATAROUTER_ROOT_GSON;
	}

	/**
	 * For sub-frameworks to provide similar classifications to this class.
	 * Not for direct use in features.
	 */
	public static Gson forDownstreamFrameworkExtension(){
		return DATAROUTER_ROOT_GSON;
	}

	/**
	 * For binding to Gson.class in an injector.
	 * Please replace with selection of a static gson instance.
	 */
	@Deprecated
	public static Gson forInjection(){
		return DATAROUTER_ROOT_GSON;
	}

	/**
	 * For using pretty print with something else parsing the data.
	 * Not easy to change like forDisplayPretty().
	 * If your output format can be changed easily, then forDisplayPretty() is better.
	 */
	public static Gson forPrettyPrint(){
		return DATAROUTER_GSON_PRETTY_PRINT;
	}

	/**
	 * For miscellaneous test usage that can be changed without breaking production.
	 */
	public static Gson forTest(){
		return DATAROUTER_ROOT_GSON;
	}

	public static Gson withEnums(EnumTypeAdapterFactory enumTypeAdapterFactory){
		return DATAROUTER_ROOT_GSON.newBuilder()
				.registerTypeAdapterFactory(enumTypeAdapterFactory)
				.create();
	}

	/**
	 * This can be used if you don't expect to serialize any enums and would like an error if an enum is found
	 */
	public static Gson withoutEnums(){
		return DATAROUTER_GSON_WITHOUT_ENUMS;
	}

	/**
	 * @deprecated This is for a quick check to see if any enums are being serialized. If so, it should be replaced with
	 *             a dedicated subclass of EnumTypeAdapterFactory. If not, it should be replaced with
	 *             GsonTool.withoutEnums().
	 */
	@Deprecated
	public static Gson withUnregisteredEnums(){
		return DATAROUTER_GSON_ALLOW_UNREGISTERED_ENUMS;
	}

	/**
	 * Quick solution for pretty-printing a value that is not parsed by another system.
	 * Lacks things like enum validation.
	 */
	public static Gson forDisplayPretty(){
		return DATAROUTER_GSON_PRETTY_PRINT;
	}

	/**
	 * Quick solution for printing a value that is not parsed by another system.
	 * Lacks things like enum validation.
	 */
	public static Gson forDisplay(){
		return DATAROUTER_ROOT_GSON;
	}

}
