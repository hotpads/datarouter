package com.hotpads.util.http.client;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.http.entity.ContentType;
import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.http.request.HotPadsHttpRequest;
import com.hotpads.util.http.request.HotPadsHttpRequest.HttpRequestMethod;

public class HotPadsHttpRequestTests {

	private static final String DTO_PARAM = "moose";
	private static final String DTO_TYPE_PARAM = "camel";
	private final HotPadsHttpClientConfig config = new HotPadsHttpClientConfig() {
		@Override
		public String getDtoParameterName(){
			return "moose";
		}

		@Override
		public String getDtoTypeParameterName(){
			return "camel";
		}
	};
	private final HotPadsHttpClient client = new HotPadsHttpClientBuilder().setConfig(config).build();

	private static final class Thing {
		@SuppressWarnings("unused")//serialized
		private String variable = "test";
		@SuppressWarnings("unused")//serialized
		private Integer number = 157;
		@SuppressWarnings("unused")//serialized
		private int primitive = 55221;
	}

	@Test
	public void testGetParams() {
		String url = "http://kittens.hotpads.com";
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.GET, url, true);
		Assert.assertEquals(url, request.getUrl());

		String expected = url + "?test=parameter";
		Map<String,String> params = new LinkedHashMap<>();
		params.put("test", "parameter");
		request.addGetParams(params);
		Assert.assertEquals(expected, request.getUrl());
		Assert.assertEquals(expected, new HotPadsHttpRequest(HttpRequestMethod.GET, expected, true).getUrl());

		expected = url + "?test=parameter&hello=world&multiple=entries";
		params = new LinkedHashMap<>();
		params.put("hello", "world");
		params.put("multiple", "entries");
		request.addGetParams(params);
		Assert.assertEquals(expected, request.getUrl());
		Assert.assertEquals(expected, new HotPadsHttpRequest(HttpRequestMethod.GET, expected, true).getUrl());

		expected = url + "?test=parameter&hello=world&multiple=entries&a=b&empty&sort";
		params = new LinkedHashMap<>();
		params.put("a", "b");
		params.put(null, null);
		params.put("     ", "meh");
		params.put("empty      ", "");
		params.put("sort", null);
		request.addGetParams(params);
		Assert.assertEquals(expected, request.getUrl());
		Assert.assertEquals(expected, new HotPadsHttpRequest(HttpRequestMethod.GET, expected, true).getUrl());

		expected = url + "?test=parameter&hello=world&multiple=entries&a=b&empty&sort&s=+%2B+%3D&%3F%26=%2F-%26";
		params = new LinkedHashMap<>();
		params.put("s", " + =");
		params.put("?&", "/-&");
		request.addGetParams(params);
		Assert.assertEquals(expected, request.getUrl());
		Assert.assertEquals(expected, new HotPadsHttpRequest(HttpRequestMethod.GET, expected, true).getUrl());

		expected = url + "?test=some%3Dvalid";
		params = Collections.singletonMap("test", "some=valid");
		request = new HotPadsHttpRequest(HttpRequestMethod.GET, url, true).addGetParams(params);
		Assert.assertEquals(expected, request.getUrl());
		Assert.assertEquals(expected, new HotPadsHttpRequest(HttpRequestMethod.GET, expected, true).getUrl());

		url = "kitty:2020?nothing=&";
		expected = "kitty:2020?nothing";
		Assert.assertEquals(expected, new HotPadsHttpRequest(HttpRequestMethod.GET, url, true).getUrl());
		Assert.assertEquals(expected, new HotPadsHttpRequest(HttpRequestMethod.GET, expected, true).getUrl());

		url = "kitty?blah=blah%3F#something++";
		expected = "kitty?blah=blah%3F";
		Assert.assertEquals(expected, new HotPadsHttpRequest(HttpRequestMethod.GET, url, true).getUrl());

		url = "kitty:2020#?nothing&blah=blah?#something++";
		expected = "kitty:2020";
		Assert.assertEquals(expected, new HotPadsHttpRequest(HttpRequestMethod.GET, url, true).getUrl());

		params = Collections.singletonMap("q", "SELECT pikachu,megaman,sonic, fifa from some.Names where thing=true");
		url = "lolcat";
		expected = "lolcat?q=SELECT+pikachu%2Cmegaman%2Csonic%2C+fifa+from+some.Names+where+thing%3Dtrue";
		Assert.assertEquals(expected, new HotPadsHttpRequest(HttpRequestMethod.GET, url, true).addGetParams(params)
				.getUrl());
		Assert.assertEquals(expected, new HotPadsHttpRequest(HttpRequestMethod.GET, expected, true).getUrl());
	}

	@Test
	public void testGetUrlFragment() {
		String url;
		HotPadsHttpRequest request;

		url = "blah?something#thing";
		request = new HotPadsHttpRequest(HttpRequestMethod.GET, url, true);
		Assert.assertEquals("thing", request.getUrlFragment());

		url = "blah?#something#thing";
		request = new HotPadsHttpRequest(HttpRequestMethod.GET, url, true);
		Assert.assertEquals("something#thing", request.getUrlFragment());

		url = "blah#?something#thing";
		request = new HotPadsHttpRequest(HttpRequestMethod.GET, url, true);
		Assert.assertEquals("?something#thing", request.getUrlFragment());
	}

	@Test
	public void testPostParams() {
		String url = "http://kittens.hotpads.com";
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.POST, url, true);

		Map<String,String> params = new LinkedHashMap<>();
		params.put("totally", "valid");
		request.addPostParams(params);
		Assert.assertEquals(params, request.getPostParams());

		request.addPostParams(params);
		request.addPostParams(params);
		request.addPostParams(params);
		Assert.assertEquals(params.size(), request.getPostParams().size());
		Assert.assertEquals(params, request.getPostParams());

		params.clear();
		params.put("", "empty");
		params.put(null, null);
		request.addPostParams(params);
		Assert.assertEquals(1, request.getPostParams().size());
	}

	@Test
	public void testAddDtosToPayload() {
		String url = "http://kittens.hotpads.com";
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.POST, url, true);

		Thing thing = new Thing();

		client.addDtoToPayload(request, Collections.singleton(thing), Thing.class.getCanonicalName());
		Map<String,String> postParams = request.getPostParams();
		String dto = postParams.get(DTO_PARAM);
		String dtoType = postParams.get(DTO_TYPE_PARAM);

		Assert.assertNotNull(dto);
		Assert.assertEquals("[{\"variable\":\"test\",\"number\":157,\"primitive\":55221}]", postParams.get(DTO_PARAM));

		Assert.assertNotNull(dtoType);
		Assert.assertEquals(Thing.class.getCanonicalName(), dtoType);
	}

	@Test
	public void testAddHeaders() {
		String url = "http://kittens.hotpads.com";
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.POST, url, true);

		Map<String,String> params = new LinkedHashMap<>();
		params.put("valid", "header");
		request.addHeaders(params);
		Assert.assertEquals(params, request.getHeaders());

		request.addHeaders(params);
		request.addHeaders(params);
		request.addHeaders(params);
		Assert.assertEquals(params.size(), request.getHeaders().size());
		Assert.assertEquals(params, request.getHeaders());

		params.clear();
		params.put("", "empty");
		params.put(null, null);
		request.addHeaders(params);
		Assert.assertEquals(1, request.getHeaders().size());

		request.setContentType(ContentType.TEXT_PLAIN);
		Assert.assertEquals(2, request.getHeaders().size());

		request.setContentType(ContentType.TEXT_HTML);
		Assert.assertEquals(2, request.getHeaders().size());
		Assert.assertEquals(2, request.getRequest().getAllHeaders().length);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullMethod() {
		new HotPadsHttpRequest(null, "http://kittens.hotpads.com", false);
	}
	@Test(expected = IllegalArgumentException.class)
	public void testNullUrl() {
		new HotPadsHttpRequest(HttpRequestMethod.HEAD, null, false);
	}
	@Test(expected = IllegalArgumentException.class)
	public void testEmptyUrl() {
		new HotPadsHttpRequest(HttpRequestMethod.HEAD, "", false);
	}
}
