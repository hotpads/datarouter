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
package io.datarouter.httpclient.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.entity.ContentType;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.httpclient.request.DatarouterHttpRequest;
import io.datarouter.httpclient.request.DatarouterHttpRequest.HttpRequestMethod;

public class DatarouterHttpRequestTests{

	private static final String URL = "http://kittens.datarouter.io";
	private static final String DTO_PARAM = "moose";
	private static final String DTO_TYPE_PARAM = "camel";
	private final DatarouterHttpClientConfig config = new DatarouterHttpClientConfig(){
		@Override
		public String getDtoParameterName(){
			return "moose";
		}

		@Override
		public String getDtoTypeParameterName(){
			return "camel";
		}
	};
	private final DatarouterHttpClient client = new DatarouterHttpClientBuilder().setConfig(config).build();

	private static final class Thing{
		@SuppressWarnings("unused")//serialized
		private String variable = "test";
		@SuppressWarnings("unused")//serialized
		private Integer number = 157;
		@SuppressWarnings("unused")//serialized
		private int primitive = 55221;
	}

	@Test
	public void testGetParams(){
		DatarouterHttpRequest request = new DatarouterHttpRequest(HttpRequestMethod.GET, URL, true);
		Assert.assertEquals(request.getUrl(), URL);

		String expected = URL + "?test=parameter";
		Map<String,String> params = new LinkedHashMap<>();
		params.put("test", "parameter");
		request.addGetParams(params);
		Assert.assertEquals(request.getUrl(), expected);
		Assert.assertEquals(new DatarouterHttpRequest(HttpRequestMethod.GET, expected, true).getUrl(), expected);

		expected = URL + "?test=parameter&hello=world&multiple=entries";
		params = new LinkedHashMap<>();
		params.put("hello", "world");
		params.put("multiple", "entries");
		request.addGetParams(params);
		Assert.assertEquals(request.getUrl(), expected);
		Assert.assertEquals(new DatarouterHttpRequest(HttpRequestMethod.GET, expected, true).getUrl(), expected);

		expected = URL + "?test=parameter&hello=world&multiple=entries&a=b&empty&sort";
		params = new LinkedHashMap<>();
		params.put("a", "b");
		params.put(null, null);
		params.put("     ", "meh");
		params.put("empty      ", "");
		params.put("sort", null);
		request.addGetParams(params);
		Assert.assertEquals(request.getUrl(), expected);
		Assert.assertEquals(new DatarouterHttpRequest(HttpRequestMethod.GET, expected, true).getUrl(), expected);

		expected = URL + "?test=parameter&hello=world&multiple=entries&a=b&empty&sort&s=+%2B+%3D&%3F%26=%2F-%26";
		params = new LinkedHashMap<>();
		params.put("s", " + =");
		params.put("?&", "/-&");
		request.addGetParams(params);
		Assert.assertEquals(request.getUrl(), expected);
		Assert.assertEquals(new DatarouterHttpRequest(HttpRequestMethod.GET, expected, true).getUrl(), expected);

		expected = URL + "?test=some%3Dvalid";
		params = Collections.singletonMap("test", "some=valid");
		request = new DatarouterHttpRequest(HttpRequestMethod.GET, URL, true).addGetParams(params);
		Assert.assertEquals(request.getUrl(), expected);
		Assert.assertEquals(new DatarouterHttpRequest(HttpRequestMethod.GET, expected, true).getUrl(), expected);

		String url = "kitty:2020?nothing=&";
		expected = "kitty:2020?nothing";
		Assert.assertEquals(new DatarouterHttpRequest(HttpRequestMethod.GET, url, true).getUrl(), expected);
		Assert.assertEquals(new DatarouterHttpRequest(HttpRequestMethod.GET, expected, true).getUrl(), expected);

		url = "kitty?blah=blah%3F#something++";
		expected = "kitty?blah=blah%3F";
		Assert.assertEquals(new DatarouterHttpRequest(HttpRequestMethod.GET, url, true).getUrl(), expected);

		url = "kitty:2020#?nothing&blah=blah?#something++";
		expected = "kitty:2020";
		Assert.assertEquals(new DatarouterHttpRequest(HttpRequestMethod.GET, url, true).getUrl(), expected);

		params = Collections.singletonMap("q", "SELECT pikachu,megaman,sonic, fifa from some.Names where thing=true");
		url = "lolcat";
		expected = "lolcat?q=SELECT+pikachu%2Cmegaman%2Csonic%2C+fifa+from+some.Names+where+thing%3Dtrue";
		Assert.assertEquals(new DatarouterHttpRequest(HttpRequestMethod.GET, url, true).addGetParams(params)
				.getUrl(), expected);
		Assert.assertEquals(new DatarouterHttpRequest(HttpRequestMethod.GET, expected, true).getUrl(), expected);
	}

	@Test
	public void testGetUrlFragment(){
		String url;
		DatarouterHttpRequest request;

		url = "blah?something#thing";
		request = new DatarouterHttpRequest(HttpRequestMethod.GET, url, true);
		Assert.assertEquals(request.getUrlFragment(), "thing");

		url = "blah?#something#thing";
		request = new DatarouterHttpRequest(HttpRequestMethod.GET, url, true);
		Assert.assertEquals(request.getUrlFragment(), "something#thing");

		url = "blah#?something#thing";
		request = new DatarouterHttpRequest(HttpRequestMethod.GET, url, true);
		Assert.assertEquals(request.getUrlFragment(), "?something#thing");
	}

	@Test
	public void testPostParams(){
		DatarouterHttpRequest request = new DatarouterHttpRequest(HttpRequestMethod.POST, URL, true);

		Map<String,List<String>> expectedParams = new LinkedHashMap<>();
		expectedParams.put("totally", Arrays.asList("valid"));
		Map<String,String> params = new LinkedHashMap<>();
		params.put("totally", "valid");
		request.addPostParams(params);
		Assert.assertEquals(request.getPostParams(), expectedParams);

		params.clear();
		params.put("", "empty");
		params.put(null, null);
		request.addPostParams(params);
		Assert.assertEquals(request.getPostParams().size(), 1);
	}

	@Test
	public void testAddDtosToPayload(){
		DatarouterHttpRequest request = new DatarouterHttpRequest(HttpRequestMethod.POST, URL, true);

		Thing thing = new Thing();

		client.addDtoToPayload(request, Collections.singleton(thing), Thing.class.getCanonicalName());
		Map<String,String> postParams = request.getFirstPostParams();
		String dto = postParams.get(DTO_PARAM);
		String dtoType = postParams.get(DTO_TYPE_PARAM);

		Assert.assertNotNull(dto);
		Assert.assertEquals(postParams.get(DTO_PARAM), "[{\"variable\":\"test\",\"number\":157,\"primitive\":55221}]");

		Assert.assertNotNull(dtoType);
		Assert.assertEquals(dtoType, Thing.class.getCanonicalName());
	}

	@Test
	public void testAddHeaders(){
		DatarouterHttpRequest request = new DatarouterHttpRequest(HttpRequestMethod.POST, URL, true);

		Map<String,List<String>> expectedHeaders = new LinkedHashMap<>();
		expectedHeaders.put("valid", Arrays.asList("header"));
		Map<String,String> headers = new LinkedHashMap<>();
		headers.put("valid", "header");
		request.addHeaders(headers);
		Assert.assertEquals(request.getHeaders(), expectedHeaders);

		headers.clear();
		headers.put("", "empty");
		headers.put(null, null);
		request.addHeaders(headers);
		Assert.assertEquals(request.getHeaders().size(), 1);

		request.setContentType(ContentType.TEXT_PLAIN);
		Assert.assertEquals(request.getHeaders().size(), 2);

		request.setContentType(ContentType.TEXT_HTML);
		Assert.assertEquals(request.getHeaders().size(), 2);
		Assert.assertEquals(request.getRequest().getAllHeaders().length, 3);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullMethod(){
		new DatarouterHttpRequest(null, URL, false);
	}
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullUrl(){
		new DatarouterHttpRequest(HttpRequestMethod.HEAD, null, false);
	}
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testEmptyUrl(){
		new DatarouterHttpRequest(HttpRequestMethod.HEAD, "", false);
	}
}
