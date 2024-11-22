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
package io.datarouter.httpclient.client;

import io.datarouter.httpclient.endpoint.java.BaseJavaEndpoint;
import io.datarouter.httpclient.endpoint.java.JavaEndpointType;
import io.datarouter.httpclient.response.Conditional;
import io.datarouter.httpclient.response.exception.DatarouterHttpException;

public interface DatarouterEndpointClient<ET extends JavaEndpointType> extends HttpPoolStats, HttpConfig{

	<R> Conditional<R> call(BaseJavaEndpoint<R,ET> endpoint);
	<R> Conditional<R> callAnyType(BaseJavaEndpoint<R,?> endpoint);
	<R> R callChecked(BaseJavaEndpoint<R,ET> endpoint) throws DatarouterHttpException;

	String toUrl(BaseJavaEndpoint<?,ET> endpoint);

}
