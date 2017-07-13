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

import java.util.function.Supplier;

public class DefaultApiKeyPredicate implements ApiKeyPredicate{

	private Supplier<String> apiKeySupplier;

	public DefaultApiKeyPredicate(Supplier<String> apiKeySupplier){
		this.apiKeySupplier = apiKeySupplier;
	}

	public DefaultApiKeyPredicate(String apiKey){
		this.apiKeySupplier = () -> apiKey;
	}

	@Override
	public boolean check(String apiKeyCandidate){
		return apiKeySupplier.get().equals(apiKeyCandidate);
	}

	@Override
	public String getApiKey(){
		return apiKeySupplier.get();
	}

}
