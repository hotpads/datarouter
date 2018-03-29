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

import io.datarouter.util.PathNode;

@Singleton
public class DatarouterWebPaths extends PathNode{

	public static final String DATAROUTER = "datarouter";

	public final DatarouterPaths datarouter = branch(DatarouterPaths.class, DATAROUTER);

	public static class DatarouterPaths extends PathNode{
		public final ClientsPaths clients = branch(ClientsPaths.class, "clients");
		public final PathNode data = leaf("data");
		public final PathNode executors = leaf("executors");
		public final PathNode memory = leaf("memory");
		public final NodesPaths nodes = branch(NodesPaths.class, "nodes");
		public final PathNode routers = leaf("routers");
		public final PathNode testApi = leaf("testApi");
		public final PathNode webAppInstances = leaf("webAppInstances");
		public final PathNode ipDetection = leaf("ipDetection");
	}

	public static class ClientsPaths extends PathNode{
		public final PathNode memory = leaf("memory");
	}

	public static class NodesPaths extends PathNode{
		public final PathNode browseData = leaf("browseData");
		public final PathNode deleteData = leaf("deleteData");
	}

}
