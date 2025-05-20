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
package io.datarouter.web.handler;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.httpclient.endpoint.param.IgnoredField;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;


public class HandlerDtoTypeTestServiceTests{

	private static class TestWithout1{
		public final String foo;
		public final Optional<String> bar;

		public TestWithout1(String foo, Optional<String> bar){
			this.foo = foo;
			this.bar = bar;
		}
	}

	private record TestWithout2(boolean foo, List<Optional<Long>> bar){}
	private record TestWithout3(TestWithout2 foo){}
	private record TestWithout4(Optional<Map<TestWithout1,TestWithout3>> foo){}

	@Test
	public void testDtosWithoutDatabeansFieldSets(){
		Assert.assertFalse(HandlerDtoTypeTestService.typeIncludeDatabeansOrFieldSet(TestWithout1.class, 0));
		Assert.assertFalse(HandlerDtoTypeTestService.typeIncludeDatabeansOrFieldSet(TestWithout2.class, 0));
		Assert.assertFalse(HandlerDtoTypeTestService.typeIncludeDatabeansOrFieldSet(TestWithout3.class, 0));
		Assert.assertFalse(HandlerDtoTypeTestService.typeIncludeDatabeansOrFieldSet(TestWithout4.class, 0));
	}

	private static class TestWith1{
		public TestDatabean databean;
	}
	private static class TestWith2{
		public TestDatabeanKey key;
	}
	private record TestWith3(TestDatabean databean){}
	private record TestWith4(TestDatabeanKey key){}
	private record TestWith5(Optional<TestDatabean> databean){}
	private record TestWith6(List<Optional<TestDatabeanKey>> key){}
	private record TestWith7(Optional<Map<String,TestWith3>> databean){}
	private static class TestWith8{
		public final TestDatabeanKey key;

		public TestWith8(TestDatabeanKey key){
			this.key = key;
		}
	}

	@Test
	public void testDtosWithDatabeansFieldSets(){
		Assert.assertTrue(HandlerDtoTypeTestService.typeIncludeDatabeansOrFieldSet(TestWith1.class, 0));
		Assert.assertTrue(HandlerDtoTypeTestService.typeIncludeDatabeansOrFieldSet(TestWith2.class, 0));
		Assert.assertTrue(HandlerDtoTypeTestService.typeIncludeDatabeansOrFieldSet(TestWith3.class, 0));
		Assert.assertTrue(HandlerDtoTypeTestService.typeIncludeDatabeansOrFieldSet(TestWith4.class, 0));
		Assert.assertTrue(HandlerDtoTypeTestService.typeIncludeDatabeansOrFieldSet(TestWith5.class, 0));
		Assert.assertTrue(HandlerDtoTypeTestService.typeIncludeDatabeansOrFieldSet(TestWith6.class, 0));
		Assert.assertTrue(HandlerDtoTypeTestService.typeIncludeDatabeansOrFieldSet(TestWith7.class, 0));
		Assert.assertTrue(HandlerDtoTypeTestService.typeIncludeDatabeansOrFieldSet(TestWith8.class, 0));
	}

	private static class TestNestingClass<T>{
		private T nested;
	}
	private record TestNestedLevel7(
			TestNestingClass<TestNestingClass<TestNestingClass<TestNestingClass<TestNestingClass<TestNestingClass<
					TestDatabean>>>>>> nested){}
	private record TestNestedLevel8(TestNestedLevel7 nested){}
	private record TestNestedField1(TestDatabean databean){}
	private record TestNestedField2(TestNestedField1 foo){}
	private record TestNestedField3(TestNestedField2 foo){}
	private record TestNestedField4(TestNestedField3 foo){}
	private record TestNestedField5(TestNestedField4 foo){}
	private record TestNestedField6(TestNestedField5 foo){}
	private record TestNestedField7(TestNestedField6 foo){}
	private record TestNestedField8(TestNestedField7 foo){}

	@Test
	public void testNestingLevel(){
		Assert.assertTrue(HandlerDtoTypeTestService.typeIncludeDatabeansOrFieldSet(TestNestedLevel7.class, 0));
		Assert.assertFalse(HandlerDtoTypeTestService.typeIncludeDatabeansOrFieldSet(TestNestedLevel8.class, 0));

		Assert.assertTrue(HandlerDtoTypeTestService.typeIncludeDatabeansOrFieldSet(TestNestedField7.class, 0));
		Assert.assertFalse(HandlerDtoTypeTestService.typeIncludeDatabeansOrFieldSet(TestNestedField8.class, 0));
	}

	private record TestCyclicalField1(TestCyclicalField2 foo){}
	private record TestCyclicalField2(TestCyclicalField1 foo){}
	private record TestSelfCyclical(TestSelfCyclical foo){}
	private record TestSelfCyclicalWithFieldSet(TestSelfCyclicalWithFieldSet foo, TestDatabeanKey key){}

	@Test
	public void testDtosWithCyclicalFields(){
		// No stack overlfow means success
		Assert.assertFalse(HandlerDtoTypeTestService.typeIncludeDatabeansOrFieldSet(TestCyclicalField1.class, 0));
		Assert.assertFalse(HandlerDtoTypeTestService.typeIncludeDatabeansOrFieldSet(TestCyclicalField2.class, 0));
		Assert.assertFalse(HandlerDtoTypeTestService.typeIncludeDatabeansOrFieldSet(TestSelfCyclical.class, 0));
		Assert.assertTrue(
				HandlerDtoTypeTestService.typeIncludeDatabeansOrFieldSet(TestSelfCyclicalWithFieldSet.class, 0));
	}

	private record TestIgnoredField1(@IgnoredField TestDatabeanKey key, String foo){}
	private record TestIgnoredField2(String foo, @IgnoredField TestDatabean databean){}
	private record TestIgnoredField3(TestIgnoredField1 foo){}

	@Test
	public void testIgnoredFields(){
		Assert.assertFalse(HandlerDtoTypeTestService.typeIncludeDatabeansOrFieldSet(TestIgnoredField1.class, 0));
		Assert.assertFalse(HandlerDtoTypeTestService.typeIncludeDatabeansOrFieldSet(TestIgnoredField2.class, 0));
		Assert.assertFalse(HandlerDtoTypeTestService.typeIncludeDatabeansOrFieldSet(TestIgnoredField3.class, 0));
	}

	private static class TestDatabeanKey extends BaseRegularPrimaryKey<TestDatabeanKey>{

		@Override
		public List<Field<?>> getFields(){
			return List.of();
		}
	}

	private static class TestDatabean extends BaseDatabean<TestDatabeanKey,TestDatabean>{

		public TestDatabean(TestDatabeanKey key){
			super(key);
		}

		@Override
		public Supplier<TestDatabeanKey> getKeySupplier(){
			return TestDatabeanKey::new;
		}

	}

}
