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
package io.datarouter.util.lang;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.Test;

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
		Assert.assertTrue(ReflectionTool.canParamsCallParamTypes(List.of(4), List.of(int.class)));
		Assert.assertTrue(ReflectionTool.canParamsCallParamTypes(List.of(4), List.of(Integer.class)));
		Assert.assertFalse(ReflectionTool.canParamsCallParamTypes(List.of("a"), List.of(int.class)));
	}

	@Test
	public void testCreateWithParameters(){
		Object[] params0 = new Object[]{new Object(), 3, 5.5d};
		Assert.assertNotNull(ReflectionTool.createWithParameters(DummyDto.class, List.of(params0)));

		Object[] params1 = new Object[]{"stringy", 3, 5.5d};
		Assert.assertNotNull(ReflectionTool.createWithParameters(DummyDto.class, List.of(params1)));
	}

	@Test(expectedExceptions = Exception.class)
	public void testCreateWithParametersInvalid(){
		Object[] params0 = new Object[]{new Object(), "square peg", 5.5d};
		DummyDto dummyDto = ReflectionTool.createWithParameters(DummyDto.class, List.of(params0));
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

	@Test
	public void testCreateNullArgsWithUnsafeAllocator(){
		ExampleDto dto = ReflectionTool.createNullArgsWithUnsafeAllocator(ExampleDto.class);
		Assert.assertNull(dto.abc);
		Assert.assertEquals(dto.def, 0);
		Assert.assertNull(dto.dummyDto);

		Example2Dto dto2 = ReflectionTool.createNullArgsWithUnsafeAllocator(Example2Dto.class);
		Assert.assertNull(dto2.abc);
		Assert.assertEquals(dto2.def, 0);
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

	public static class TypesExampleDto2{

		public final String str;
		public Optional<String> optionalString = Optional.empty();
		public Optional<String> optionalString2;

		public TypesExampleDto2(String str){
			this.str = str;
			this.optionalString2 = Optional.empty();
		}

	}

	@Test
	public void testCreateWithoutNoArgsWithOptionals(){
		TypesExampleDto2 dto = ReflectionTool.createWithoutNoArgs(TypesExampleDto2.class);
		Assert.assertNotNull(dto.optionalString);
		Assert.assertNotNull(dto.optionalString2);
	}

	public static class TypesExampleDto3{

		public final String str;

		public TypesExampleDto3(String str){
			Objects.requireNonNull(str);
			this.str = str;
		}

	}

	@Test
	public void testCreateWithoutNoArgsWithConstructorChecks(){
		@SuppressWarnings("unused")
		TypesExampleDto3 dto = ReflectionTool.createNullArgsWithUnsafeAllocator(TypesExampleDto3.class);
	}

	private static class InnerToStringTestDto{
		final String inner1;
		final double[] inner2;

		public InnerToStringTestDto(String inner1, double[] inner2){
			this.inner1 = inner1;
			this.inner2 = inner2;
		}

		public String toStringEclipse(){
			return "InnerToStringTestDto [inner1=" + this.inner1 + ", inner2=" + Arrays.toString(this.inner2) + "]";
		}

		@Override
		public String toString(){
			return ReflectionTool.toString(this);
		}


	}

	private static class OuterToStringTestDto{
		final int outer1;
		final InnerToStringTestDto outer2;
		final String outer3;

		public OuterToStringTestDto(int outer1, InnerToStringTestDto outer2, String outer3){
			this.outer1 = outer1;
			this.outer2 = outer2;
			this.outer3 = outer3;
		}

		public String toStringEclipse(){
			return "OuterToStringTestDto [outer1=" + this.outer1 + ", outer2=" + this.outer2 + ", outer3=" + this.outer3
					+ "]";
		}

		@Override
		public String toString(){
			return ReflectionTool.toString(this);
		}
	}

	@Test
	public void testToString(){
		var inner = new InnerToStringTestDto("yogurt", new double[]{1.2, 1.3});
		Assert.assertEquals(inner.toString(), inner.toStringEclipse());
		String actualInner = inner.toString();
		String expectedInner = String.format("InnerToStringTestDto [inner1=%s, inner2=%s]",
				inner.inner1,
				Arrays.toString(inner.inner2));
		Assert.assertEquals(actualInner, expectedInner);

		var outer = new OuterToStringTestDto(5, inner, "fridge");
		Assert.assertEquals(outer.toString(), outer.toStringEclipse());
		String actualOuter = outer.toString();
		String expectedOuter = String.format("OuterToStringTestDto [outer1=%s, outer2=%s, outer3=%s]",
				outer.outer1,
				outer.outer2,
				outer.outer3);
		Assert.assertEquals(actualOuter, expectedOuter);
	}

}
