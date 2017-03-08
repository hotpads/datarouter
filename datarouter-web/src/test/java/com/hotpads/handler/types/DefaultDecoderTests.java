package com.hotpads.handler.types;

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

import com.hotpads.handler.TestApiHandler;
import com.hotpads.handler.TestApiHandler.FooBar;
import com.hotpads.util.http.json.GsonJsonSerializer;

public class DefaultDecoderTests{

	private static final DefaultDecoder decoder = new DefaultDecoder(new GsonJsonSerializer());

	private static final String firstname = "Goulven";
	private static final String lastname = "Cornec";
	private static final Boolean booleanValue = true;
	private static final int intValue = 42;
	private static final Date created = new Date();
	private static final String createdString = DateTimeFormatter.ISO_INSTANT.format(created.toInstant());
	private static final String kenavo = "kenavo";
	private static final int[] intArray = new int[]{1, 2, 3, 125};
	private static final int base = 2;
	private static final String[] stringArray = new String[]{"degemer", "mat", "ar", "gouel"};

	// Used via reflection in testMethodParameterNameInclusionAtRuntime
	@SuppressWarnings("unused")
	private void myMethod(String myParameter){}

	@Test
	public void testMethodParameterNameInclusionAtRuntime() throws NoSuchMethodException, SecurityException{
		Method method = DefaultDecoderTests.class.getDeclaredMethod("myMethod", String.class);
		Assert.assertNotNull(method);
		Assert.assertEquals("myParameter", method.getParameters()[0].getName());
	}

	@Test
	public void testBefore() throws NoSuchMethodException, SecurityException{
		Method method = TestApiHandler.class.getMethod("before");
		HttpServletRequest request = new HttpRequestBuilder().build();
		Object[] args = decoder.decode(request, method);
		Assert.assertEquals(args.length, 0);
	}

	@Test
	public void testHello() throws NoSuchMethodException, SecurityException{
		Method method = TestApiHandler.class.getMethod("hello", String.class);
		HttpServletRequest request = new HttpRequestBuilder()
				.withParameter("_first_name", firstname)
				.build();
		Object[] args = decoder.decode(request, method);
		Assert.assertEquals(args.length, 1);
		Assert.assertEquals(args[0], firstname);
	}

	@Test
	public void testHi() throws NoSuchMethodException, SecurityException{
		Method method = TestApiHandler.class.getMethod("hi", String.class, String.class, Boolean.TYPE);
		HttpServletRequest request = new HttpRequestBuilder()
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
		HttpServletRequest request = new HttpRequestBuilder()
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
		HttpServletRequest request = new HttpRequestBuilder()
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
		HttpServletRequest request = new HttpRequestBuilder()
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
		HttpServletRequest request = new HttpRequestBuilder()
				.withBody(kenavo)
				.build();
		Object[] args = decoder.decode(request, method);
		Assert.assertEquals(args.length, 1);
		Assert.assertEquals(args[0], kenavo);
	}

	@Test
	public void testSumInBase() throws NoSuchMethodException, SecurityException{
		Method method = TestApiHandler.class.getMethod("sumInBase", int[].class, Integer.TYPE);
		HttpServletRequest request = new HttpRequestBuilder()
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
		HttpServletRequest request = new HttpRequestBuilder()
				.withBody(Arrays.toString(stringArray))
				.withParameter("needle", stringArray[2])
				.build();
		Object[] args = decoder.decode(request, method);
		Assert.assertEquals(args.length, 3);
		Assert.assertEquals(args[0], new HashSet<>(Arrays.asList(stringArray)));
		Assert.assertEquals(args[1], stringArray[2]);
		Assert.assertEquals(args[2], Arrays.toString(stringArray));
	}

}
