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
package io.datarouter.web.handler.types;

import java.lang.reflect.Method;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.gson.GsonJsonSerializer;
import io.datarouter.gson.GsonTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.handler.TestApiHandler;
import io.datarouter.web.handler.TestApiHandler.FooBar;
import io.datarouter.web.handler.types.TestWebApiHandler.PrintIntList;
import io.datarouter.web.util.http.MockHttpServletRequestBuilder;

public class DefaultDecoderTests{

	private static final DefaultDecoder decoder = new DefaultDecoder(new GsonJsonSerializer(GsonTool.withoutEnums()));

	private static final String firstname = "Goulven";
	private static final String lastname = "Cornec";
	private static final Boolean booleanValue = true;
	private static final int intValue = 42;
	private static final Date created = new Date();
	private static final String createdString = DateTimeFormatter.ISO_INSTANT.format(created.toInstant());
	private static final String kenavo = "kenavo";
	private static final int[] intArray = {1, 2, 3, 125};
	private static final int base = 2;
	private static final String[] stringArray = {"degemer", "mat", "ar", "gouel"};

	// Used via reflection in testMethodParameterNameInclusionAtRuntime
	@SuppressWarnings("unused")
	private void myMethod(String myParameter){
	}

	@Test
	public void testMethodParameterNameInclusionAtRuntime() throws NoSuchMethodException, SecurityException{
		Method method = DefaultDecoderTests.class.getDeclaredMethod("myMethod", String.class);
		Assert.assertNotNull(method);
		Assert.assertEquals("myParameter", method.getParameters()[0].getName());
	}

	@Test
	public void testBefore() throws NoSuchMethodException, SecurityException{
		Method method = TestApiHandler.class.getMethod("before");
		HttpServletRequest request = new MockHttpServletRequestBuilder().build();
		Object[] args = decoder.decode(request, method);
		Assert.assertEquals(args.length, 0);
	}

	@Test
	public void testHello() throws NoSuchMethodException, SecurityException{
		Method method = TestApiHandler.class.getMethod("hello", String.class);
		HttpServletRequest request = new MockHttpServletRequestBuilder()
				.withParameter("_first_name", firstname)
				.build();
		Object[] args = decoder.decode(request, method);
		Assert.assertEquals(args.length, 1);
		Assert.assertEquals(args[0], firstname);
	}

	@Test
	public void testHi() throws NoSuchMethodException, SecurityException{
		Method method = TestApiHandler.class.getMethod("hi", String.class, String.class, Boolean.TYPE);
		HttpServletRequest request = new MockHttpServletRequestBuilder()
				.withParameter("firstname", firstname)
				.withParameter("lastname", lastname)
				.withParameter("english", booleanValue.toString())
				.build();
		Object[] args = decoder.decode(request, method);
		Assert.assertEquals(args.length, 3);
		Assert.assertEquals(args[0], firstname);
		Assert.assertEquals(args[1], lastname);
		Assert.assertEquals(args[2], booleanValue);
	}

	@Test
	public void testDescribe() throws NoSuchMethodException, SecurityException{
		Method method = TestApiHandler.class.getMethod("describe", FooBar.class);
		HttpServletRequest request = new MockHttpServletRequestBuilder()
				.withParameter("fooBar", "{firstField:'" + firstname + "',intField:" + intValue + ",created:'"
						+ createdString + "'}")
				.build();
		Object[] args = decoder.decode(request, method);
		Assert.assertEquals(args.length, 1);
		Assert.assertEquals(args[0].getClass(), FooBar.class);
		FooBar fooBar = (FooBar)args[0];
		Assert.assertEquals(fooBar.firstField, firstname);
		Assert.assertEquals(fooBar.intField, intValue);
		Assert.assertEquals(fooBar.created, created);
	}


	@Test
	public void testCount() throws NoSuchMethodException, SecurityException{
		Method method = TestApiHandler.class.getMethod("count", Collection.class);
		HttpServletRequest request = new MockHttpServletRequestBuilder()
				.withParameter("fooBars", "[{firstField:'" + firstname + "',intField:" + intValue + ",created:'"
						+ createdString + "'}]")
				.build();
		Object[] args = decoder.decode(request, method);
		Assert.assertEquals(args.length, 1);
		Assert.assertTrue(Collection.class.isAssignableFrom(args[0].getClass()));
		Collection<?> collection = (Collection<?>)args[0];
		Assert.assertEquals(collection.size(), 1);
		Object item = collection.iterator().next();
		Assert.assertEquals(item.getClass(), FooBar.class);
		FooBar fooBar = (FooBar)item;
		Assert.assertEquals(fooBar.firstField, firstname);
		Assert.assertEquals(fooBar.intField, intValue);
		Assert.assertEquals(fooBar.created, created);
	}

	@Test
	public void testSize() throws NoSuchMethodException, SecurityException{
		Method method = TestApiHandler.class.getMethod("size", List.class);
		HttpServletRequest request = new MockHttpServletRequestBuilder()
				.withBody("[{},{}]")
				.build();
		Object[] args = decoder.decode(request, method);
		Assert.assertEquals(args.length, 1);
		Assert.assertTrue(List.class.isAssignableFrom(args[0].getClass()));
		List<?> list = (List<?>)args[0];
		Assert.assertEquals(list.size(), 2);
	}

	@Test
	public void testLength() throws NoSuchMethodException, SecurityException{
		Method method = TestApiHandler.class.getMethod("length", String.class);
		HttpServletRequest request = new MockHttpServletRequestBuilder()
				.withBody(kenavo)
				.build();
		Object[] args = decoder.decode(request, method);
		Assert.assertEquals(args.length, 1);
		Assert.assertEquals(args[0], kenavo);
	}

	@Test
	public void testSumInBase() throws NoSuchMethodException, SecurityException{
		Method method = TestApiHandler.class.getMethod("sumInBase", int[].class, Integer.TYPE);
		HttpServletRequest request = new MockHttpServletRequestBuilder()
				.withBody(Arrays.toString(intArray))
				.withParameter("base", Integer.toString(base))
				.build();
		Object[] args = decoder.decode(request, method);
		Assert.assertEquals(args.length, 2);
		Assert.assertEquals(args[0], intArray);
		Assert.assertEquals(args[1], base);
	}

	@Test
	public void testTimeContains() throws NoSuchMethodException, SecurityException{
		Method method = TestApiHandler.class.getMethod("timeContains", Set.class, String.class, String.class);
		HttpServletRequest request = new MockHttpServletRequestBuilder()
				.withBody(Arrays.toString(stringArray))
				.withParameter("needle", stringArray[2])
				.build();
		Object[] args = decoder.decode(request, method);
		Assert.assertEquals(args.length, 3);
		Assert.assertEquals(args[0], Scanner.of(stringArray).collect(HashSet::new));
		Assert.assertEquals(args[1], stringArray[2]);
		Assert.assertEquals(args[2], Arrays.toString(stringArray));
	}

	/* multi value processing*/

	@Test
	public void testPrimitiveFormEncodedArrays() throws NoSuchMethodException, SecurityException{
		Method method = TestApiHandler.class.getMethod("printPrimitiveIntArray", int[].class);
		HttpServletRequest request = new MockHttpServletRequestBuilder()
				.withArrayParameter("numbers[]", "1", "2", "3")
				.build();

		Object[] args = decoder.decode(request, method);
		Assert.assertEquals(args.length, 1);
		Assert.assertTrue(args[0] instanceof int[]);
		Assert.assertEquals(args[0], new int[]{1, 2, 3});
	}

	@Test
	public void testObjectFormEncodedArrays() throws NoSuchMethodException, SecurityException{
		Method method = TestApiHandler.class.getMethod("printIntegerObjectArray", Integer[].class);
		HttpServletRequest request = new MockHttpServletRequestBuilder()
				.withArrayParameter("numbers[]", "1", "2", "3")
				.build();

		Object[] args = decoder.decode(request, method);
		Assert.assertEquals(args.length, 1);
		Assert.assertTrue(args[0] instanceof Integer[]);
		Assert.assertEquals(args[0], new Integer[]{1, 2, 3});
	}

	@Test
	public void testSingleElementFormEncodedArray() throws NoSuchMethodException, SecurityException{
		Method method = TestApiHandler.class.getMethod("printIntegerObjectArray", Integer[].class);
		HttpServletRequest request = new MockHttpServletRequestBuilder()
				.withArrayParameter("numbers[]", "1")
				.build();

		Object[] args = decoder.decode(request, method);
		Assert.assertEquals(args.length, 1);
		Assert.assertTrue(args[0] instanceof Integer[]);
		Assert.assertEquals(args[0], new Integer[]{1});
	}

	@Test
	public void testNoParamNameFormEncodedArray() throws NoSuchMethodException, SecurityException{
		Method method = TestApiHandler.class.getMethod("printPrimitiveIntArrayNoParamName", int[].class);
		HttpServletRequest request = new MockHttpServletRequestBuilder()
				.withArrayParameter("numbers[]", "1", "2", "3")
				.build();

		Object[] args = decoder.decode(request, method);
		Assert.assertEquals(args.length, 1);
		Assert.assertTrue(args[0] instanceof int[]);
		Assert.assertEquals(args[0], new int[]{1, 2, 3});
	}

	// broken, need IN-10619
	@Test(enabled = false)
	public void testPrintIntListNoParamName() throws NoSuchMethodException, SecurityException{
		Method method = TestApiHandler.class.getMethod("printIntListNoParamName", List.class);
		HttpServletRequest request = new MockHttpServletRequestBuilder()
				.withArrayParameter("numbers[]", "1", "2", "3")
				.build();

		Object[] args = decoder.decode(request, method);
		Assert.assertEquals(args.length, 1);
		Assert.assertTrue(args[0] instanceof int[]);
		Assert.assertEquals(args[0], new int[]{1, 2, 3});
	}

	@Test
	public void testPrintIntListWebApi() throws NoSuchMethodException, SecurityException{
		Method method = TestWebApiHandler.class.getMethod("printIntList", PrintIntList.class);
		HttpServletRequest request = new MockHttpServletRequestBuilder()
				.withMethod("GET")
				.withArrayParameter("numbers[]", "1", "2", "3")
				.build();

		Object[] args = decoder.decode(request, method);
		Assert.assertEquals(args.length, 1);
		Assert.assertEquals(((PrintIntList)args[0]).numbers, Arrays.asList(1, 2, 3));
	}

	@Test
	public void testPrintIntListWebApiJsonStyle() throws NoSuchMethodException, SecurityException{
		Method method = TestWebApiHandler.class.getMethod("printIntList", PrintIntList.class);
		HttpServletRequest request = new MockHttpServletRequestBuilder()
				.withMethod("GET")
				.withArrayParameter("numbers", "[1,2,3]")
				.build();

		Object[] args = decoder.decode(request, method);
		Assert.assertEquals(args.length, 1);
		Assert.assertEquals(((PrintIntList)args[0]).numbers, Arrays.asList(1, 2, 3));
	}

	@Test
	public void testComplexFormEncodedArray() throws NoSuchMethodException, SecurityException{
		Method method = TestApiHandler.class.getMethod("printComplicatedArrayParams", int[].class, Integer[].class,
				int[].class);
		HttpServletRequest request = new MockHttpServletRequestBuilder()
				.withArrayParameter("foo", "1", "2", "3")
				.withParameter("bar", "[1,2]")
				.withArrayParameter("baz[]", "1")
				.build();

		Object[] args = decoder.decode(request, method);
		Assert.assertEquals(args.length, 3);

		Assert.assertTrue(args[0] instanceof int[]);
		Assert.assertTrue(args[1] instanceof Integer[]);
		Assert.assertTrue(args[2] instanceof int[]);

		Assert.assertEquals(args[0], new int[]{1, 2, 3});
		Assert.assertEquals(args[1], new Integer[]{1, 2});
		Assert.assertEquals(args[2], new int[]{1});
	}


}
