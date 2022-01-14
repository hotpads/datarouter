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
package io.datarouter.bytes.binarydto;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.Java9;
import io.datarouter.bytes.binarydto.codec.BinaryDtoCodec;
import io.datarouter.bytes.binarydto.dto.BaseBinaryDto;
import io.datarouter.scanner.Scanner;

public class BinaryDtoNestedTests{

	public static class InnerTestDto extends BaseBinaryDto{
		public final int f1;
		public final Float f2;

		public InnerTestDto(int f1, Float f2){
			this.f1 = f1;
			this.f2 = f2;
		}
	}

	public static class OuterTestDto extends BaseBinaryDto{
		public final InnerTestDto inner;
		public final InnerTestDto inner2;
		public final InnerTestDto[] innerArray;
		public final List<InnerTestDto> innerList;
		public final List<InnerTestDto> innerList2;
		public final List<InnerTestDto> innerList3;

		public OuterTestDto(
				InnerTestDto inner,
				InnerTestDto inner2,
				InnerTestDto[] innerArray,
				List<InnerTestDto> innerList,
				List<InnerTestDto> innerList2,
				List<InnerTestDto> innerList3){
			this.inner = inner;
			this.inner2 = inner2;
			this.innerArray = innerArray;
			this.innerList = innerList;
			this.innerList2 = innerList2;
			this.innerList3 = innerList3;
		}

	}

	@Test
	public void testAssignability(){
		Assert.assertTrue(BaseBinaryDto.class.isAssignableFrom(InnerTestDto.class));
	}

	@Test
	public void testInnerType() throws NoSuchFieldException, SecurityException{
		Field field = OuterTestDto.class.getDeclaredField("inner");
		Class<?> itemClass = field.getType();
		Assert.assertEquals(itemClass.getTypeName(), InnerTestDto.class.getTypeName());
	}

	@Test
	public void testArrayType() throws NoSuchFieldException, SecurityException{
		Field field = OuterTestDto.class.getDeclaredField("innerArray");
		Class<?> arrayClass = field.getType();
		Assert.assertTrue(arrayClass.isArray());
		Class<?> itemClass = arrayClass.getComponentType();
		Assert.assertEquals(itemClass.getTypeName(), InnerTestDto.class.getTypeName());
	}

	@Test
	public void testListType() throws NoSuchFieldException, SecurityException, ClassNotFoundException{
		Field field = OuterTestDto.class.getDeclaredField("innerList");
		Type genericType = field.getGenericType();
		ParameterizedType parameterizedType = (ParameterizedType)genericType;
		Type outerType = parameterizedType.getRawType();
		Assert.assertEquals(outerType.getTypeName(), List.class.getTypeName());
		Type innerType = parameterizedType.getActualTypeArguments()[0];
		Assert.assertEquals(innerType.getTypeName(), InnerTestDto.class.getTypeName());
		Class<?> innerClass = Class.forName(innerType.getTypeName());
		Assert.assertEquals(innerClass.getTypeName(), InnerTestDto.class.getTypeName());
	}

	@Test
	public void testCreateCodec(){
		new BinaryDtoCodec<>(OuterTestDto.class);
	}

	@Test
	public void testCodec(){
		InnerTestDto inner0 = new InnerTestDto(0, 0f);
		InnerTestDto inner1 = new InnerTestDto(1, 1f);
		InnerTestDto inner2 = new InnerTestDto(2, null);
		InnerTestDto inner3 = new InnerTestDto(3, 3f);
		InnerTestDto[] innerDtos = new InnerTestDto[]{inner0, inner1, inner2, inner3};

		BinaryDtoCodec<InnerTestDto> innerCodec = new BinaryDtoCodec<>(InnerTestDto.class);
		List<byte[]> innersBytes = Scanner.of(innerDtos)
				.map(innerCodec::encode)
				.list();

		OuterTestDto outerDto = new OuterTestDto(
				inner0,
				null,
				new InnerTestDto[]{inner0, inner1, null},
				Arrays.asList(inner2, null, inner3),
				Collections.emptyList(),
				null);

		List<byte[]> expectedByteSegments = Java9.listOf(
				//inner
				new byte[]{1},//item present
				innersBytes.get(0),//item0
				//inner1
				new byte[]{0},//null item
				//innerArray
				new byte[]{1},//array present
				new byte[]{3},//array length 3
				new byte[]{1},//item present
				innersBytes.get(0),//item0
				new byte[]{1},//item present
				innersBytes.get(1),//item1
				new byte[]{0},//item null
				//innerList
				new byte[]{1},//list present
				new byte[]{3},//list length 3
				new byte[]{1},//item present
				innersBytes.get(2),//item2
				new byte[]{0},//null item
				new byte[]{1},//item present
				innersBytes.get(3),//item3
				//innerList2
				new byte[]{1},//list present
				new byte[]{0},//list length 3
				//innerList3
				new byte[]{0});//list null
		byte[] expectedFullBytes = ByteTool.concatenate(expectedByteSegments);

		BinaryDtoCodec<OuterTestDto> outerCodec = new BinaryDtoCodec<>(OuterTestDto.class);
		byte[] actualFullBytes = outerCodec.encode(outerDto);
		Assert.assertEquals(actualFullBytes, expectedFullBytes);

		OuterTestDto actualOuterDto = outerCodec.decode(actualFullBytes);
		Assert.assertEquals(actualOuterDto, outerDto);
	}

}
