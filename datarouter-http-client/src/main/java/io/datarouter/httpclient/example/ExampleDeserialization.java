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
package io.datarouter.httpclient.example;

import io.datarouter.httpclient.client.DatarouterHttpClient;
import io.datarouter.httpclient.client.DatarouterHttpClientBuilder;
import io.datarouter.httpclient.request.DatarouterHttpRequest;
import io.datarouter.httpclient.request.DatarouterHttpRequest.HttpRequestMethod;

public class ExampleDeserialization{

	public static class ExampleDataTransferObject{
		public Long id;
		public String title;
	}

	public static void main(String[] args){
		DatarouterHttpRequest request = new DatarouterHttpRequest(
				HttpRequestMethod.GET,
				"https://example.com/api",
				true);
		request.addGetParam("id", "1");
		DatarouterHttpClient client = new DatarouterHttpClientBuilder().build();
		ExampleDataTransferObject dto = client.execute(request, ExampleDataTransferObject.class);
	}

}
