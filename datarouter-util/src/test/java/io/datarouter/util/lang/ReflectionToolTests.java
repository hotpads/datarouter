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
package io.datarouter.util.lang;

import java.lang.reflect.Method;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.Java9;
import io.datarouter.util.Require;

public class ReflectionToolTests{

	public static class DummyDto{
		public final Object field0;
		public final int field1;
		public final Double field2;

		public DummyDto(Object field0, int field1, Double field2){
			this.field0 = field0;
			this.field1 = field1;
			this.field2 = field2;
		}

		@SuppressWarnings("unused") // referenced by reflection
		private void parentMethod(){
		}
	}

	public static class ExtensionDto extends DummyDto{
		public final long field3;

		public ExtensionDto(Object field0, int field1, Double field2, long field3){
			super(field0, field1, field2);
			this.field3 = field3;
		}

		@SuppressWarnings("unused") // referenced by reflection
		private void childMethod(){
		}
	}

	@Test
	public void testCanParamsCallParamTypes(){
		Assert.assertTrue(ReflectionTool.canParamsCallParamTypes(Java9.listOf(4), Java9.listOf(int.class)));
		Assert.assertTrue(ReflectionTool.canParamsCallParamTypes(Java9.listOf(4), Java9.listOf(Integer.class)));
		Assert.assertFalse(ReflectionTool.canParamsCallParamTypes(Java9.listOf("a"), Java9.listOf(int.class)));
	}

	@Test
	public void testCreateWithParameters(){
		Object[] params0 = new Object[]{new Object(), 3, 5.5d};
		Assert.assertNotNull(ReflectionTool.createWithParameters(DummyDto.class, Java9.listOf(params0)));

		Object[] params1 = new Object[]{"stringy", 3, 5.5d};
		Assert.assertNotNull(ReflectionTool.createWithParameters(DummyDto.class, Java9.listOf(params1)));
	}

	@Test(expectedExceptions = Exception.class)
	public void testCreateWithParametersInvalid(){
		Object[] params0 = new Object[]{new Object(), "square peg", 5.5d};
		DummyDto dummyDto = ReflectionTool.createWithParameters(DummyDto.class, Java9.listOf(params0));
		Assert.assertNotNull(dummyDto);
	}

	@Test
	public void testGetDeclaredFieldsFromAncestors(){
		Assert.assertEquals(ReflectionTool.getDeclaredFieldsFromAncestors(ExtensionDto.class).size(), 3);
	}

	@Test
	public void testGetDeclaredFieldsIncludingAncestors(){
		Assert.assertEquals(ReflectionTool.getDeclaredFieldsIncludingAncestors(ExtensionDto.class).size(), 4);
	}

	@Test
	public void testGetDeclaredFields(){
		Assert.assertEquals(ReflectionTool.getDeclaredFields(ExtensionDto.class).size(), 1);
	}

	@Test
	public void testGetDeclaredMethodsIncludingAncestors(){
		List<String> methods = ReflectionTool.getDeclaredMethodsIncludingAncestors(ExtensionDto.class)
				.map(Method::getName)
				.list();
		Require.contains(methods, "childMethod");
		Require.contains(methods, "parentMethod");
	}

	public static class ExampleDto{

		public final String abc;
		public final long def;
		public final DummyDto dummyDto;

		public ExampleDto(String abc, long def, DummyDto dummyDto){
			this.abc = abc;
			this.def = def;
			this.dummyDto = dummyDto;
		}

	}

	public static class Example2Dto{

		public final String abc;
		public final long def;
		public final DummyDto dummyDto;

		public Example2Dto(){
			this.abc = "hi";
			this.def = 5;
			this.dummyDto = null;
		}

	}

	@Test
	public void testCreateWithoutNoArgs(){
		ExampleDto dto = ReflectionTool.createWithoutNoArgs(ExampleDto.class);
		Assert.assertNull(dto.abc);
		Assert.assertEquals(dto.def, 0);
		Assert.assertNull(dto.dummyDto);

		Example2Dto dto2 = ReflectionTool.createWithoutNoArgs(Example2Dto.class);
		Assert.assertEquals(dto2.abc, "hi");
		Assert.assertEquals(dto2.def, 5);
		Assert.assertNull(dto2.dummyDto);
	}

	// p - primitive
	// b - boxed
	public static class TypesExampleDto{

		public final byte pByte;
		public final short pShort;
		public final int pInt;
		public final long pLong;
		public final float pFloat;
		public final double pDouble;
		public final char pChar;
		public final boolean pBoolean;

		public final byte[] pByteArray;

		public final Byte bByte;
		public final Short bShort;
		public final Integer bInt;
		public final Long bLong;
		public final Float bFloat;
		public final Double bDouble;
		public final Character bChar;
		public final Boolean bBoolean;

		public final String string;
		public final Object object;

		public TypesExampleDto(
				byte pByte,
				short pShort,
				int pInt,
				long pLong,
				float pFloat,
				double pDouble,
				char pChar,
				boolean pBoolean,

				byte[] pByteArray,

				Byte bByte,
				Short bShort,
				Integer bInt,
				Long bLong,
				Float bFloat,
				Double bDouble,
				Character bChar,
				Boolean bBoolean,

				String string,
				Object object){
			this.pByte = pByte;
			this.pShort = pShort;
			this.pInt = pInt;
			this.pLong = pLong;
			this.pFloat = pFloat;
			this.pDouble = pDouble;
			this.pChar = pChar;
			this.pBoolean = pBoolean;

			this.pByteArray = pByteArray;

			this.bByte = bByte;
			this.bShort = bShort;
			this.bInt = bInt;
			this.bLong = bLong;
			this.bFloat = bFloat;
			this.bDouble = bDouble;
			this.bChar = bChar;
			this.bBoolean = bBoolean;

			this.string = string;
			this.object = object;
		}

	}

	@Test
	public void testCreateWithoutNoArgsWithPrimitives(){
		TypesExampleDto dto = ReflectionTool.createWithoutNoArgs(TypesExampleDto.class);

		Assert.assertEquals(dto.pByte, 0);
		Assert.assertEquals(dto.pShort, 0);
		Assert.assertEquals(dto.pInt, 0);
		Assert.assertEquals(dto.pLong, 0);
		Assert.assertEquals(dto.pFloat, 0);
		Assert.assertEquals(dto.pDouble, 0);
		Assert.assertEquals(dto.pChar, '\u0000');
		Assert.assertFalse(dto.pBoolean);

		Assert.assertNull(dto.pByteArray);

		Assert.assertNull(dto.bByte);
		Assert.assertNull(dto.bShort);
		Assert.assertNull(dto.bInt);
		Assert.assertNull(dto.bLong);
		Assert.assertNull(dto.bFloat);
		Assert.assertNull(dto.bDouble);
		Assert.assertNull(dto.bChar);
		Assert.assertNull(dto.bBoolean);

		Assert.assertNull(dto.string);
		Assert.assertNull(dto.object);
	}

}
