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
package io.datarouter.websocket.session;

import java.time.Duration;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.gson.GsonJsonSerializer;
import io.datarouter.httpclient.client.BaseDatarouterHttpClientWrapper;
import io.datarouter.httpclient.client.DatarouterHttpClientBuilder;
import io.datarouter.httpclient.security.DefaultCsrfGenerator;
import io.datarouter.httpclient.security.DefaultSignatureGenerator;

@Singleton
public class PushServiceHttpClient extends BaseDatarouterHttpClientWrapper{

	@Inject
	public PushServiceHttpClient(PushServiceSettingsSupplier settings){
		super(new DatarouterHttpClientBuilder(GsonJsonSerializer.DEFAULT)
				.setSignatureGenerator(new DefaultSignatureGenerator(settings::getSalt))
				.setCsrfGenerator(new DefaultCsrfGenerator(settings::getCipherKey))
				.setApiKeySupplier(settings::getApiKey)
				// fail fast on dead server but keep normal response wait timeout
				.setConnectTimeoutMs(Duration.ofMillis(50))
				.build());
	}

}
