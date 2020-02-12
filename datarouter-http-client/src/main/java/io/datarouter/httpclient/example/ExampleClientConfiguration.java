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

import com.google.gson.GsonBuilder;

import io.datarouter.httpclient.client.DatarouterHttpClient;
import io.datarouter.httpclient.client.DatarouterHttpClientBuilder;
import io.datarouter.httpclient.json.GsonJsonSerializer;
import io.datarouter.httpclient.json.JsonSerializer;

public class ExampleClientConfiguration{

	// By default, datarouter-http-client uses vanilla gson.
	// You can implement the JsonSerializer interface or create a GsonJsonSerializer.
	JsonSerializer jsonSerializer = new GsonJsonSerializer(new GsonBuilder()
			.serializeNulls()
			.create());

	DatarouterHttpClient httpClient = new DatarouterHttpClientBuilder()
			// Retry the requests twice
			.setRetryCount(() -> 2)
			// Add apiKey=SECRET to each request
			.setApiKeySupplier(() -> "SECRET")
			// Change the maximum number of connections in the pool
			.setMaxConnectionsPerRoute(100)
			.setMaxTotalConnections(100)
			// Ignore invalid SSL certificates
			.setIgnoreSsl(true)
			// Use a custom JSON serializer
			.setJsonSerializer(jsonSerializer)
			.build();

}
