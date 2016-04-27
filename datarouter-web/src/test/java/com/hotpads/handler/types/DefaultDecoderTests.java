package com.hotpads.handler.types;

import java.lang.reflect.Method;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.test.TestApiHandler;
import com.hotpads.datarouter.test.TestApiHandler.FooBar;
import com.hotpads.util.http.json.GsonJsonSerializer;

public class DefaultDecoderTests{

	private static final DefaultDecoder decoder = new DefaultDecoder(new GsonJsonSerializer());

	private static final String firstname = "Goulven";
	private static final String lastname = "Cornec";
	private static final Boolean booleanValue = true;
	private static final int intValue = 42;
	private static final Date created = new Date();
	private static final String createdString = DateTimeFormatter.ISO_INSTANT.format(created.toInstant());

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
		HttpServletRequest request = new HttpRequestBuilder().withParameter("_first_name", firstname).build();
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
		System.out.println(args);
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

}
