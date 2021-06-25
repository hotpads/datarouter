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

import java.util.function.Supplier;

import io.datarouter.httpclient.endpoint.BaseEndpoint;
import io.datarouter.httpclient.endpoint.DatarouterServiceHealthcheckEndpoint;
import io.datarouter.httpclient.response.Conditional;

//currently works as a mixin for existing BaseDatarouterEndpointHttpClientWrappers
//TODO (later) refactor this to stop extending DatarouterHttpClient and instead include/exclude certain methods from it
/**
 * for use specifically with servers that are built on datarouter-web
 */
public interface DatarouterServiceHttpClient extends DatarouterHttpClient{

	default Conditional<Object> checkHealth(){
		return call(DatarouterServiceHealthcheckEndpoint.getEndpoint());
	}

	default <E> Conditional<E> callWithHealthcheck(BaseEndpoint<E> endpoint, Supplier<Boolean> shouldCheckHealth){
		return shouldCheckHealth.get() ? callWithHealthcheck(endpoint) : call(endpoint);
	}

	default <E> Conditional<E> callWithHealthcheck(BaseEndpoint<E> endpoint){
		return checkHealth()
				.map($ -> call(endpoint))
				.orElse(Conditional.failure(new RuntimeException("healtcheck failed")));
	}

}
