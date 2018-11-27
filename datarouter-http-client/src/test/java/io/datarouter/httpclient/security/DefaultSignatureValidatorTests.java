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
package io.datarouter.httpclient.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.datarouter.httpclient.request.CachingHttpServletRequest;
import io.datarouter.httpclient.request.CachingServletInputStream;

public class DefaultSignatureValidatorTests{

	private Map<String,String> params;
	private DefaultSignatureValidator validator;

	@BeforeTest
	public void beforeTest(){
		validator = new DefaultSignatureValidator(() -> "329jfsJLKFj2fjjfL2319Jvn2332we");

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
		String originalSignature = validator.getHexSignature(params);
		Map<String,String> reorderedParams = new HashMap<>(params);
		String reorderedSignature = validator.getHexSignature(reorderedParams);

		Assert.assertEquals(originalSignature, reorderedSignature);
	}

	@Test
	public void testCheckHexSignatureEntity(){
		StringEntity entity = new StringEntity("{ 'key': 'value' }", StandardCharsets.UTF_8);
		String hexSignature = validator.getHexSignature(params, entity);
		Assert.assertTrue(validator.checkHexSignature(params, entity, hexSignature));
		Assert.assertFalse(validator.checkHexSignature(params, null, hexSignature));
	}

	@Test
	public void testValidateFormPost(){
		String hexSignature = validator.getHexSignature(params);
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.doReturn(hexSignature).when(request).getParameter(SecurityParameters.SIGNATURE);
		Mockito.doReturn("POST").when(request).getMethod();
		Mockito.doReturn(ContentType.APPLICATION_FORM_URLENCODED.getMimeType()).when(request).getContentType();
		Mockito.doReturn(toParamMap(params)).when(request).getParameterMap();

		SecurityValidationResult result = validator.validate(request);
		Assert.assertTrue(result.isSuccess());

		Mockito.doReturn("foobar").when(request).getParameter(SecurityParameters.SIGNATURE);
		result = validator.validate(request);
		Assert.assertFalse(result.isSuccess());
	}

	@Test
	public void testValidateJsonPost() throws IOException{
		StringEntity entity = new StringEntity("{ 'key': 'value' }", StandardCharsets.UTF_8);
		String hexSignature = validator.getHexSignature(params, entity);
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.doReturn(new CachingServletInputStream(entity.getContent())).when(request).getInputStream();
		Mockito.doReturn(hexSignature).when(request).getParameter(SecurityParameters.SIGNATURE);
		Mockito.doReturn("POST").when(request).getMethod();
		Mockito.doReturn(ContentType.APPLICATION_JSON.getMimeType()).when(request).getContentType();
		Mockito.doReturn(toParamMap(params)).when(request).getParameterMap();

		SecurityValidationResult result = validator.validate(request);
		Assert.assertTrue(result.isSuccess());

		Mockito.doReturn("foobar").when(request).getParameter(SecurityParameters.SIGNATURE);
		result = validator.validate(request);
		Assert.assertFalse(result.isSuccess());
	}

	@Test
	public void testValidateAlreadyCached() throws IOException{
		byte[] bytes = "{ 'key': 'value' }".getBytes(StandardCharsets.UTF_8);
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.doReturn(new CachingServletInputStream(new ByteArrayInputStream(bytes))).when(request).getInputStream();
		Mockito.doReturn(toParamMap(params)).when(request).getParameterMap();
		CachingHttpServletRequest cachingRequest = CachingHttpServletRequest.getOrCreate(request);
		Assert.assertNotNull(cachingRequest.getContent());
		Mockito.reset(request);

		SecurityValidationResult result = validator.validate(cachingRequest);
		Mockito.verify(request, Mockito.never()).getInputStream();
		Assert.assertSame(result.getWrappedRequest(), cachingRequest);
	}

	private static Map<String,String[]> toParamMap(Map<String,String> input){
		Map<String,String[]> output = new HashMap<>();
		input.forEach((key, value) -> output.put(key, new String[]{value}));
		return output;
	}

}