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
package io.datarouter.web.handler.documentation;

import java.lang.reflect.Type;
import java.net.URI;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.datarouter.gson.DatarouterGsons;
import io.datarouter.gson.GsonJsonSerializer;
import io.datarouter.scanner.Scanner;
import io.datarouter.types.MilliTime;
import io.datarouter.types.Ulid;
import io.datarouter.types.UlidReversed;
import io.datarouter.web.handler.documentation.DocumentedExampleDto.DocumentedExampleEnumDto;
import io.datarouter.web.handler.encoder.JsonAwareHandlerCodec;

@SuppressWarnings("serial")
public class ApiDocServiceTests{

	private static final JsonAwareHandlerCodec JSON_CODEC = () -> new GsonJsonSerializer(DatarouterGsons.forTest());

	@Test
	public void testCreateBestExamplePrimitives(){
		testExample(boolean.class, new DocumentedExampleDto(false));
		testExample(Boolean.class, new DocumentedExampleDto(false));
		testExample(int.class, new DocumentedExampleDto(0));
		testExample(Double.class, new DocumentedExampleDto(0.0d));
		testExample(String.class, new DocumentedExampleDto(""));
	}

	@Test
	public void testCreateBestExampleSpecial(){
		Scanner.of(
				JsonArray.class,
				JsonObject.class,
				UUID.class,
				Ulid.class,
				UlidReversed.class,
				Date.class,
				Instant.class,
				MilliTime.class,
				URI.class)
				.forEach(ApiDocServiceTests::testClassIsHandled);
	}

	@Test
	public void testCreateBestExampleCollections(){
		testExample(new TypeToken<List<String>>(){}.getType(), new DocumentedExampleDto(List.of("")));
		testExample(new TypeToken<Set<String>>(){}.getType(), new DocumentedExampleDto(Set.of("")));
	}

	@Test
	public void testCreateBestExampleDto(){
		record Nested(Nested nested){}
		record Dto(String name, int age, boolean lovesJava, Nested nested){}
		testExample(Dto.class, new DocumentedExampleDto(new Dto("", 0, false, new Nested(null))));
	}

	@Test
	public void testCreateBestExampleEnum(){
		enum MyEnum{A, B, C}
		testExample(MyEnum.class, new DocumentedExampleDto(MyEnum.A, Set.of(
				new DocumentedExampleEnumDto("MyEnum", "\"A\",\"B\",\"C\""))));
	}

	private static void testExample(Type input, DocumentedExampleDto expected){
		DocumentedExampleDto example = createExample(input);
		Assert.assertEquals(example, expected);
	}

	private static void testClassIsHandled(Class<?> input){
		DocumentedExampleDto example = createExample(input);
		Assert.assertEquals(input, example.exampleObject().getClass());
	}

	private static DocumentedExampleDto createExample(Type input){
		return ApiDocService.createBestExample(
				JSON_CODEC,
				input,
				Set.of(),
				0);
	}

}
