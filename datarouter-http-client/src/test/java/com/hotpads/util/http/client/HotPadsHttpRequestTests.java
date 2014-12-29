package com.hotpads.util.http.client;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.Assert;

import org.apache.http.entity.ContentType;
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
		private String variable = "test";
		private Integer number = 157;
		private int primitive = 55221;
		
		public String getVariable() {
			return variable;
		}
		public void setVariable(String variable) {
			this.variable = variable;
		}
		public Integer getNumber() {
			return number;
		}
		public void setNumber(Integer number) {
			this.number = number;
		}
		public int getPrimitive() {
			return primitive;
		}
		public void setPrimitive(int primitive) {
			this.primitive = primitive;
		}
	}
	
	@Test
	public void testGetParams() {
		String url = "http://kittens.hotpads.com";
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.GET, url, true);
		Assert.assertEquals(url, request.getRequest().getURI().toString());
		
		String expected = url + "?test=parameter";
		Map<String,String> params = new LinkedHashMap<>();
		params.put("test", "parameter");
		request.addGetParams(params);
		Assert.assertEquals(expected, request.getRequest().getURI().toString());
		Assert.assertEquals(expected, new HotPadsHttpRequest(HttpRequestMethod.GET, expected, true).getRequest()
				.getURI().toString());

		expected = url + "?test=parameter&hello=world&multiple=entries";
		params = new LinkedHashMap<>();
		params.put("hello", "world");
		params.put("multiple", "entries");
		request.addGetParams(params);
		Assert.assertEquals(expected, request.getRequest().getURI().toString());
		Assert.assertEquals(expected, new HotPadsHttpRequest(HttpRequestMethod.GET, expected, true).getRequest()
				.getURI().toString());
		
		expected = url + "?test=parameter&hello=world&multiple=entries&a=b&empty&sort";
		params = new LinkedHashMap<>();
		params.put("a", "b");
		params.put(null, null);
		params.put("     ", "meh");
		params.put("empty      ", "");
		params.put("sort", "");
		request.addGetParams(params);
		Assert.assertEquals(expected, request.getRequest().getURI().toString());
		Assert.assertEquals(expected, new HotPadsHttpRequest(HttpRequestMethod.GET, expected, true).getRequest()
				.getURI().toString());
		
		expected = url + "?test=parameter&hello=world&multiple=entries&a=b&empty&sort&s=+%2B+%3D&%3F%26=%2F-%26";
		params = new LinkedHashMap<>();
		params.put("s", " + =");
		params.put("?&", "/-&");
		request.addGetParams(params);
		Assert.assertEquals(expected, request.getRequest().getURI().toString());
		Assert.assertEquals(expected, new HotPadsHttpRequest(HttpRequestMethod.GET, expected, true).getRequest()
				.getURI().toString());
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
		
		client.addDtosToPayload(request, Collections.singleton(thing), Thing.class.getCanonicalName());
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
