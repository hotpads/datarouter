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

import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.gson.DatarouterGsons;
import io.datarouter.gson.GsonJsonSerializer;
import io.datarouter.httpclient.endpoint.param.IgnoredField;
import io.datarouter.httpclient.response.ApiResponseDto;
import io.datarouter.instrumentation.doc.ApiDoc;
import io.datarouter.instrumentation.typescript.TsNullable;
import io.datarouter.web.handler.encoder.JsonAwareHandlerCodec;

public class ApiDocSchemaToolTests{

	private static final JsonAwareHandlerCodec JSON_CODEC = () -> new GsonJsonSerializer(DatarouterGsons.forTest());

	public static class TestClassPrimitiveField{
		public int primitiveField;
	}

	public static class TestClassParametrizedField{
		@ApiDoc(
				isOptional = true,
				description = "A primitive field",
				max = 100,
				min = 0,
				maxLength = 255)
		public int primitiveField;
		@TsNullable
		@ApiDoc(description = "A list of strings")
		List<String> stringList;
		@ApiDoc(description = "A list of list of strings")
		List<List<String>> nestedStringList;
		List<TestEnum> nestedEnumList;
		Map<String, Integer> testMap;
		List<TestClassPrimitiveField> nestedPrimitiveField;
		ApiResponseDto<TestClassPrimitiveField> apiResponse;
	}

	public record StatsRecord(
			Integer count,
			Map<String, MinMaxRecord> fields,
			Map<String,Map<String, StatsRecord>> subgroups){
	}

	public record MinMaxRecord(
			Number min,
			Number max){
	}

	public static class TestClassStringField{
		public String stringField;
	}

	public static enum TestEnum{
		VALUE1, VALUE2
	}

	public static class TestClassEnumField{
		public TestEnum enumField;
	}

	public static class NestedClass{
		public Integer nestedField;
	}

	public static class TestClassNestedClass{
		public NestedClass nestedClass;
	}

	public static class TestClassIgnoredField{
		@IgnoredField
		public int ignoredField;
	}

	public static class ArrayTest{
		public TestClassNestedClass[] arrayField;
		public List<TestClassNestedClass> listField;
		public TestClassNestedClass[] arrayField2;
		private Map<String,Object>[] questions;
		private String[][] answers;
	}

	public static class TestClassStaticField{
		public static int staticField;
	}

	public static class ComprehensiveTestClass{
		public int primitiveField;
		public String stringField;
		public TestEnum enumField;
		public NestedClass nestedClass;
		@IgnoredField
		public int ignoredField;
		public static int staticField;
	}


	@Test
	public void testTypeOverrides(){
		ApiDocSchemaDto schema = ApiDocSchemaTool.buildSchemaFromDto(TestClassStringField.class, JSON_CODEC, Map.of());
		Assert.assertEquals(schema.getName(), "TestClassStringField");
		Assert.assertEquals(schema.getFields().size(), 1);
		ApiDocSchemaDto field = schema.getFields().getFirst();
		Assert.assertEquals(field.getName(), "stringField");
		Assert.assertEquals(field.getType(), "string");
		Map<Class<?>, String> typeOverrides = Map.of(String.class, "number");
		schema = ApiDocSchemaTool.buildSchemaFromDto(TestClassStringField.class, JSON_CODEC, typeOverrides);
		Assert.assertEquals(schema.getName(), "TestClassStringField");
		Assert.assertEquals(schema.getFields().size(), 1);
		ApiDocSchemaDto overriddenField = schema.getFields().getFirst();
		Assert.assertEquals(overriddenField.getName(), "stringField");
		Assert.assertEquals(overriddenField.getType(), "number");
	}

	@Test
	public void testCycle(){
		ApiDocSchemaDto schema = ApiDocSchemaTool.buildSchemaFromDto(StatsRecord.class, JSON_CODEC, Map.of());
		Assert.assertNotNull(schema);
	}

	@Test
	public void testArrayField(){
		ApiDocSchemaDto schema = ApiDocSchemaTool.buildSchemaFromDto(ArrayTest.class, JSON_CODEC, Map.of());
		Assert.assertEquals(schema.getName(), "ArrayTest");
		Assert.assertEquals(schema.getFields().size(), 5);
		ApiDocSchemaDto field = schema.getFields().getFirst();
		Assert.assertEquals(field.getName(), "arrayField");
		Assert.assertTrue(field.isArray());
		Assert.assertEquals(field.getFields().size(), 1);
		Assert.assertEquals(field.getFields().getFirst().getName(), "nestedClass");
		ApiDocSchemaDto listField = schema.getFields().get(1);
		Assert.assertEquals(listField.getName(), "listField");
		Assert.assertEquals(listField.getType(), "parameter");
	}

	@Test
	public void testBuildSchemaPrimitiveField(){
		ApiDocSchemaDto schema = ApiDocSchemaTool.buildSchemaFromDto(TestClassPrimitiveField.class, JSON_CODEC,
				Map.of());
		Assert.assertEquals(schema.getName(), "TestClassPrimitiveField");
		Assert.assertEquals(schema.getFields().size(), 1);
		ApiDocSchemaDto field = schema.getFields().getFirst();
		Assert.assertEquals(field.getName(), "primitiveField");
		Assert.assertEquals(field.getType(), "number");
	}

	@Test
	public void testBuildParametrizedField(){
		ApiDocSchemaDto schema = ApiDocSchemaTool.buildSchemaFromDto(TestClassParametrizedField.class, JSON_CODEC,
				Map.of());
		Assert.assertEquals(schema.getName(), "TestClassParametrizedField");
		Assert.assertEquals(schema.getFields().size(), 7);
		ApiDocSchemaDto field = schema.getFields().getFirst();
		Assert.assertEquals(field.getName(), "primitiveField");
		Assert.assertEquals(field.getType(), "number");
		Assert.assertTrue(field.isOptional());
		Assert.assertEquals(field.getDescription(), "A primitive field");
		Assert.assertEquals(field.getMax(), 100);
		Assert.assertEquals(field.getMin(), 0);
		Assert.assertEquals(field.getMaxLength(), 255);
		ApiDocSchemaDto listField = schema.getFields().get(1);
		Assert.assertEquals(listField.getName(), "stringList");
		Assert.assertTrue(listField.isOptional());
		Assert.assertEquals(listField.getType(), "parameter");
		Assert.assertEquals(listField.getDescription(), "A list of strings");
		Assert.assertNull(listField.getFields());
		ApiDocSchemaDto listFieldField = listField.getParameters().getFirst();
		Assert.assertEquals(listFieldField.getName(), "string");
		Assert.assertEquals(listFieldField.getType(), "string");
		Assert.assertNull(listFieldField.getParameters());
		ApiDocSchemaDto nestedStringListField = schema.getFields().get(2);
		Assert.assertEquals(nestedStringListField.getName(), "nestedStringList");
		Assert.assertEquals(nestedStringListField.getType(), "parameter");
		Assert.assertEquals(nestedStringListField.getDescription(), "A list of list of strings");
	}

	@Test
	public void testBuildSchemaStringField(){
		ApiDocSchemaDto schema = ApiDocSchemaTool.buildSchemaFromDto(TestClassStringField.class, JSON_CODEC, Map.of());
		Assert.assertEquals(schema.getName(), "TestClassStringField");
		Assert.assertEquals(schema.getFields().size(), 1);
		ApiDocSchemaDto field = schema.getFields().getFirst();
		Assert.assertEquals(field.getName(), "stringField");
		Assert.assertEquals(field.getType(), "string");
	}

	@Test
	public void testBuildSchemaEnumField(){
		ApiDocSchemaDto schema = ApiDocSchemaTool.buildSchemaFromDto(TestClassEnumField.class, JSON_CODEC, Map.of());
		Assert.assertEquals(schema.getName(), "TestClassEnumField");
		Assert.assertEquals(schema.getFields().size(), 1);
		ApiDocSchemaDto field = schema.getFields().getFirst();
		Assert.assertEquals(field.getName(), "enumField");
		Assert.assertEquals(field.getType(), "enum");
		Assert.assertEquals(field.getEnumValues().size(), 2);
		Assert.assertEquals(field.getEnumValues().getFirst(), "\"VALUE1\"");
		Assert.assertEquals(field.getEnumValues().get(1), "\"VALUE2\"");
	}

	@Test
	public void testBuildSchemaNestedClass(){
		ApiDocSchemaDto schema = ApiDocSchemaTool.buildSchemaFromDto(TestClassNestedClass.class, JSON_CODEC, Map.of());
		Assert.assertEquals(schema.getName(), "TestClassNestedClass");
		Assert.assertEquals(schema.getFields().size(), 1);
		ApiDocSchemaDto field = schema.getFields().getFirst();
		Assert.assertEquals(field.getName(), "nestedClass");
		Assert.assertEquals(field.getType(), "object");
		Assert.assertEquals(field.getFields().size(), 1);
		ApiDocSchemaDto nestedField = field.getFields().getFirst();
		Assert.assertEquals(nestedField.getName(), "nestedField");
		Assert.assertEquals(nestedField.getType(), "number");
	}

	@Test
	public void testBuildSchemaIgnoredField(){
		ApiDocSchemaDto schema = ApiDocSchemaTool.buildSchemaFromDto(TestClassIgnoredField.class, JSON_CODEC, Map.of());
		Assert.assertEquals(schema.getName(), "TestClassIgnoredField");
		Assert.assertEquals(schema.getFields().size(), 0);
	}

	@Test
	public void testBuildSchemaStaticField(){
		ApiDocSchemaDto schema = ApiDocSchemaTool.buildSchemaFromDto(TestClassStaticField.class, JSON_CODEC, Map.of());
		Assert.assertEquals(schema.getName(), "TestClassStaticField");
		Assert.assertEquals(schema.getFields().size(), 0);
	}

	@Test
	public void testBuildSchema_Comprehensive(){
		// Build the schema
		ApiDocSchemaDto schema =
				ApiDocSchemaTool.buildSchemaFromDto(ComprehensiveTestClass.class, JSON_CODEC, Map.of());

		// Verify the schema
		Assert.assertEquals(schema.getName(), "ComprehensiveTestClass");
		Assert.assertEquals(schema.getFields().size(), 4);

		// Verify the primitive field
		ApiDocSchemaDto primitiveField = schema.getFields().getFirst();
		Assert.assertEquals(primitiveField.getName(), "primitiveField");
		Assert.assertEquals(primitiveField.getType(), "number");

		// Verify the string field
		ApiDocSchemaDto stringField = schema.getFields().get(1);
		Assert.assertEquals(stringField.getName(), "stringField");
		Assert.assertEquals(stringField.getType(), "string");

		// Verify the enum field
		ApiDocSchemaDto enumField = schema.getFields().get(2);
		Assert.assertEquals(enumField.getName(), "enumField");
		Assert.assertEquals(enumField.getType(), "enum");
		Assert.assertEquals(enumField.getEnumValues().size(), 2);
		Assert.assertEquals(enumField.getEnumValues().getFirst(), "\"VALUE1\"");
		Assert.assertEquals(enumField.getEnumValues().get(1), "\"VALUE2\"");

		// Verify the nested class
		ApiDocSchemaDto nestedClass = schema.getFields().get(3);
		Assert.assertEquals(nestedClass.getName(), "nestedClass");
		Assert.assertEquals(nestedClass.getType(), "object");
		Assert.assertEquals(nestedClass.getFields().size(), 1);
		ApiDocSchemaDto nestedField = nestedClass.getFields().getFirst();
		Assert.assertEquals(nestedField.getName(), "nestedField");
		Assert.assertEquals(nestedField.getType(), "number");
	}
}
