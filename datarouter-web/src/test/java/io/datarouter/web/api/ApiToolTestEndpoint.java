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
package io.datarouter.web.api;

import io.datarouter.httpclient.endpoint.java.BaseJavaEndpoint;
import io.datarouter.httpclient.endpoint.java.JavaEndpointType.NoOpEndpointType;
import io.datarouter.httpclient.request.HttpRequestMethod;
import io.datarouter.pathnode.PathNode;

public abstract class ApiToolTestEndpoint<R> extends BaseJavaEndpoint<R,NoOpEndpointType>{

	public ApiToolTestEndpoint(HttpRequestMethod method, PathNode pathNode){
		super(method, pathNode);
	}

}