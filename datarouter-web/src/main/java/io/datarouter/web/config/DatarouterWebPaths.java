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

import io.datarouter.pathnode.PathNode;
import io.datarouter.pathnode.PathsRoot;

@Singleton
public class DatarouterWebPaths extends PathNode implements PathsRoot{

	public static final String DATAROUTER = "datarouter";

	public final DatarouterPaths datarouter = branch(DatarouterPaths::new, DATAROUTER);
	public final PermissionRequestPaths permissionRequest = branch(PermissionRequestPaths::new, "permissionRequest");
	public final SigninPaths signin = branch(SigninPaths::new, "signin");
	public final DocumentationPaths documentation = branch(DocumentationPaths::new, "documentation");

	public final PathNode consumer = leaf("consumer");
	public final PathNode keepalive = leaf("keepalive");
	public final PathNode signout = leaf("signout");

	public static class DatarouterPaths extends PathNode{
		public final ClientPaths client = branch(ClientPaths::new, "client");
		public final InfoPaths info = branch(InfoPaths::new, "info");
		public final NodesPaths nodes = branch(NodesPaths::new, "nodes");

		public final PathNode deployment = leaf("deployment");
		public final PathNode emailTest = leaf("emailTest");
		public final PathNode executors = leaf("executors");
		public final PathNode ipDetection = leaf("ipDetection");
		public final PathNode memory = leaf("memory");
		public final PathNode settings = leaf("settings");
		public final PathNode shutdown = leaf("shutdown");
		public final PathNode tableConfiguration = leaf("tableConfiguration");
		public final PathNode testApi = leaf("testApi");
	}

	public static class ClientPaths extends PathNode{
		public final PathNode inspectClient = leaf("inspectClient");
		public final PathNode initClient = leaf("initClient");
		public final PathNode initAllClients = leaf("initAllClients");
	}

	public static class NodesPaths extends PathNode{
		public final PathNode browseData = leaf("browseData");
		public final PathNode deleteData = leaf("deleteData");
		public final PathNode getData = leaf("getData");
		public final PathNode search = leaf("search");
	}

	public static class InfoPaths extends PathNode{
		public final PathNode listeners = leaf("listeners");
		public final PathNode routeSets = leaf("routeSets");
		public final PathNode filterParams = leaf("filterParams");
		public final PathNode plugins = leaf("plugins");
		public final PathNode properties = leaf("properties");
	}

	public static class PermissionRequestPaths extends PathNode{
		public final PathNode declineAll = leaf("declineAll");
		public final PathNode declinePermissionRequests = leaf("declinePermissionRequests");
		public final PathNode showForm = leaf("showForm");
		public final PathNode submit = leaf("submit");
	}

	public static class SigninPaths extends PathNode{
		public final PathNode submit = leaf("submit");
	}

	public static class DocumentationPaths extends PathNode{
		public final PathNode readmes = leaf("readmes");
		public final PathNode systemDocs = leaf("systemDocs");
	}

}
