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
package io.datarouter.web.util.http;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.entity.InputStreamEntity;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CachingHttpServletRequestTests{

	@Test
	public void test() throws IOException{
		String sentBody = "{ 'key': 'value' }";
		Charset charset = StandardCharsets.UTF_8;
		HttpServletRequest request = new MockHttpServletRequestBuilder()
				.withBody(sentBody)
				.build();

		CachingHttpServletRequest cachingRequest = CachingHttpServletRequest.getOrCreate(request);
		// test repeat reads
		for(int i = 0; i < 5; i++){
			String receivedBody = EntityUtils.toString(new InputStreamEntity(cachingRequest.getInputStream()), charset);
			Assert.assertEquals(sentBody, receivedBody);
		}

		Assert.assertSame(CachingHttpServletRequest.getOrCreate(cachingRequest), cachingRequest);
	}

}
