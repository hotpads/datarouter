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
package io.datarouter.binarydto;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.BaseBinaryDto;
import io.datarouter.binarydto.dto.BinaryDto;
import io.datarouter.binarydto.dto.ComparableBinaryDto;

public class BinaryDtoNestedTests{

	@SuppressWarnings("unused")
	private static class InnerTestDto extends ComparableBinaryDto<InnerTestDto>{
		public final int f1;
		public final Float f2;

		public InnerTestDto(int f1, Float f2){
			this.f1 = f1;
			this.f2 = f2;
		}
	}

	//this doesn't really make sense as a key, but let's see what happens
	@SuppressWarnings("unused")
	private static class OuterTestDto extends BinaryDto<OuterTestDto>{
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
		Assert.assertTrue(ComparableBinaryDto.class.isAssignableFrom(InnerTestDto.class));
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
		BinaryDtoIndexedCodec.of(OuterTestDto.class);
	}

	@Test
	public void testEncode(){
		var inner0 = new InnerTestDto(0, 0f);
		var inner1 = new InnerTestDto(1, 1f);
		var inner2 = new InnerTestDto(2, null);
		var inner3 = new InnerTestDto(3, 3f);

		var outerDto = new OuterTestDto(
				inner0,
				null,
				new InnerTestDto[]{inner0, inner1, null},
				Arrays.asList(inner2, null, inner3),
				List.of(),
				null);

		Assert.assertEquals(outerDto.cloneIndexed(), outerDto);
	}

}
