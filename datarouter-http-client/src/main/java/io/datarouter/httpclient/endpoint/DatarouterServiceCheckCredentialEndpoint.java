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
package io.datarouter.httpclient.endpoint;

import io.datarouter.httpclient.DatarouterServicePaths;
import io.datarouter.httpclient.dto.DatarouterAccountCredentialStatusDto;
import io.datarouter.httpclient.endpoint.EndpointType.NoOpEndpointType;
import io.datarouter.pathnode.PathNode;

public class DatarouterServiceCheckCredentialEndpoint
extends BaseEndpoint<DatarouterAccountCredentialStatusDto,NoOpEndpointType>{

	private static final PathNode PATHS = new DatarouterServicePaths().datarouter.api.accounts.checkCredential;

	private DatarouterServiceCheckCredentialEndpoint(){
		super(GET, PATHS, true, false, true);
	}

	public static DatarouterServiceCheckCredentialEndpoint getEndpoint(){
		return new DatarouterServiceCheckCredentialEndpoint();
	}

}
