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
package io.datarouter.httpclient;

import javax.inject.Singleton;

import io.datarouter.pathnode.PathNode;
import io.datarouter.pathnode.PathsRoot;

@Singleton
public class DatarouterServicePaths extends PathNode implements PathsRoot{

	public final DatarouterServiceSubPaths datarouter = branch(DatarouterServiceSubPaths::new, "datarouter");

	public static class DatarouterServiceSubPaths extends PathNode{
		public final ApiPaths api = branch(ApiPaths::new, "api");
		public final PathNode healthcheck = leaf("healthcheck");
	}

	public static class ApiPaths extends PathNode{
		public final AccountsPaths accounts = branch(AccountsPaths::new, "accounts");
	}

	public static class AccountsPaths extends PathNode{
		public final PathNode checkCredential = leaf("checkCredential");
	}

}
