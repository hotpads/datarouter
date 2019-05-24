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
package io.datarouter.web.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.datarouter.httpclient.HttpHeaders;
import io.datarouter.httpclient.security.DefaultSignatureGenerator;
import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.web.util.http.CachingHttpServletRequest;
import io.datarouter.web.util.http.MockHttpServletRequestBuilder;

public class DefaultSignatureValidatorTests{

	private Map<String,String> params;
	private DefaultSignatureGenerator generator;
	private DefaultSignatureValidator validator;

	@BeforeTest
	public void beforeTest(){
		generator = new DefaultSignatureGenerator(() -> "329jfsJLKFj2fjjfL2319Jvn2332we");
		validator = new DefaultSignatureValidator(generator);

		params = new LinkedHashMap<>();
		params.put(SecurityParameters.SIGNATURE, "foobar");
		params.put("submitAction", "doSomething");
		params.put("param1", "test1");
		params.put("param2", "test2");
		params.put("paramWithoutValue", null);
		params.put("csrfToken", "B8kgUfjdsa1234jsl9sdfkJ==");
		params.put("apiKey", "jklfds90j2r13kjJfjklJF923j2rjLKJfjs");
		params.put("csrfIv", "x92jfjJdslSJFj29lsfjsf==");
		params = Collections.unmodifiableMap(params);
	}

	@Test
	public void testSettingParameterOrder(){
		String originalSignature = generator.getHexSignature(params);
		Map<String,String> reorderedParams = new HashMap<>(params);
		String reorderedSignature = generator.getHexSignature(reorderedParams);

		Assert.assertEquals(originalSignature, reorderedSignature);
	}

	@Test
	public void testCheckHexSignatureEntity(){
		StringEntity entity = new StringEntity("{ 'key': 'value' }", StandardCharsets.UTF_8);
		String hexSignature = generator.getHexSignature(params, entity);
		Assert.assertTrue(validator.checkHexSignature(params, entity, hexSignature));
		Assert.assertFalse(validator.checkHexSignature(params, null, hexSignature));
	}

	@Test
	public void testValidateFormPost(){
		String hexSignature = generator.getHexSignature(params);
		MockHttpServletRequestBuilder requestBuilder = new MockHttpServletRequestBuilder()
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.getMimeType())
				.withMethod("POST")
				.withParameters(toParamMap(params))
				.withParameter(SecurityParameters.SIGNATURE, hexSignature);
		SecurityValidationResult result = validator.validate(requestBuilder.build());
		Assert.assertTrue(result.isSuccess());
		requestBuilder.withParameter(SecurityParameters.SIGNATURE, "foobar");
		result = validator.validate(requestBuilder.build());
		Assert.assertFalse(result.isSuccess());
	}

	@Test
	public void testValidateJsonPost(){
		String body = "{ 'key': 'value' }";
		StringEntity entity = new StringEntity(body, StandardCharsets.UTF_8);
		String hexSignature = generator.getHexSignature(params, entity);
		MockHttpServletRequestBuilder requestBuilder = new MockHttpServletRequestBuilder()
				.withBody(body)
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withMethod("POST")
				.withParameters(toParamMap(params))
				.withParameter(SecurityParameters.SIGNATURE, hexSignature);
		SecurityValidationResult result = validator.validate(requestBuilder.build());
		Assert.assertTrue(result.isSuccess());
		requestBuilder.withParameter(SecurityParameters.SIGNATURE, "foobar");
		result = validator.validate(requestBuilder.build());
		Assert.assertFalse(result.isSuccess());
	}

	@Test
	public void testValidateAlreadyCached() throws IOException{
		String body = "{ 'key': 'value' }";
		MockHttpServletRequestBuilder requestBuilder = new MockHttpServletRequestBuilder()
				.withBody(body)
				.withParameters(toParamMap(params));
		CachingHttpServletRequest cachingRequest = CachingHttpServletRequest.getOrCreate(requestBuilder.build());
		Assert.assertNotNull(cachingRequest.getContent());
		SecurityValidationResult result = validator.validate(cachingRequest);
		Assert.assertSame(result.getWrappedRequest(), cachingRequest);
	}

	private static Map<String,String[]> toParamMap(Map<String,String> input){
		Map<String,String[]> output = new HashMap<>();
		input.forEach((key, value) -> output.put(key, new String[]{value}));
		return output;
	}

}