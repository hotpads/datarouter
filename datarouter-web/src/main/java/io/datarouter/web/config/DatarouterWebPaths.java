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
package io.datarouter.web.config;

import javax.inject.Singleton;

import io.datarouter.httpclient.path.PathNode;

@Singleton
public class DatarouterWebPaths extends PathNode{

	public static final String DATAROUTER = "datarouter";

	public final DatarouterPaths datarouter = branch(DatarouterPaths::new, DATAROUTER);

	public static class DatarouterPaths extends PathNode{
		public final ClientsPaths clients = branch(ClientsPaths::new, "clients");
		public final PathNode data = leaf("data");
		public final PathNode executors = leaf("executors");
		public final PathNode memory = leaf("memory");
		public final NodesPaths nodes = branch(NodesPaths::new, "nodes");
		public final PathNode testApi = leaf("testApi");
		public final PathNode webappInstances = leaf("webappInstances");
		public final PathNode ipDetection = leaf("ipDetection");
		public final PathNode deployment = leaf("deployment");
	}

	public static class ClientsPaths extends PathNode{
		public final PathNode memory = leaf("memory");
	}

	public static class NodesPaths extends PathNode{
		public final PathNode browseData = leaf("browseData");
		public final PathNode deleteData = leaf("deleteData");
		public final PathNode getData = leaf("getData");
	}

}
