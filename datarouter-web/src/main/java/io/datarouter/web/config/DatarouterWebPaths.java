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
package io.datarouter.web.config;

import io.datarouter.pathnode.PathNode;
import io.datarouter.pathnode.PathsRoot;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterWebPaths extends PathNode implements PathsRoot{

	public static final String DATAROUTER = "datarouter";

	public final DatarouterPaths datarouter = branch(DatarouterPaths::new, DATAROUTER);
	public final DocumentationPaths documentation = branch(DocumentationPaths::new, "documentation");

	public final PathNode consumer = leaf("consumer");
	public final PathNode keepalive = leaf("keepalive");
	public final PathNode signout = leaf("signout");

	public static class DatarouterPaths extends PathNode{
		public final ClientPaths client = branch(ClientPaths::new, "client");
		public final InfoPaths info = branch(InfoPaths::new, "info");
		public final ExecutorPaths executors = branch(ExecutorPaths::new, "executors");
		public final NodesPaths nodes = branch(NodesPaths::new, "nodes");
		public final TestApiPaths testApi = branch(TestApiPaths::new, "testApi");
		public final MemoryPaths memory = branch(MemoryPaths::new, "memory");

		public final PathNode deployment = leaf("deployment");
		public final PathNode emailTest = leaf("emailTest");
		public final HttpPaths http = branch(HttpPaths::new, "http");
		public final PathNode ipDetection = leaf("ipDetection");
		public final PathNode settings = leaf("settings");
		public final PathNode shutdown = leaf("shutdown");
		public final HandlerPaths handler = branch(HandlerPaths::new, "handler");
		public final PathNode envVars = leaf("envVars");
	}

	public static class HandlerPaths extends PathNode{
		public final PathNode handlerSearch = leaf("handlerSearch");
	}

	public static class HttpPaths extends PathNode{
		public final PathNode tester = leaf("tester");
		public final PathNode dnsLookup = leaf("dnsLookup");
		public final PathNode reuseTester = leaf("reuseTester");
	}

	public static class ExecutorPaths extends PathNode{
		public final PathNode getExecutors = leaf("getExecutors");
	}

	public static class MemoryPaths extends PathNode{
		public final PathNode garbageCollector = leaf("garbageCollector");
		public final PathNode view = leaf("view");
	}

	public static class ClientPaths extends PathNode{
		public final PathNode inspectClient = leaf("inspectClient");
		public final PathNode initClient = leaf("initClient");
		public final PathNode initAllClients = leaf("initAllClients");
	}

	public static class NodesPaths extends PathNode{
		public final BrowseDataPaths browseData = branch(BrowseDataPaths::new, "browseData");
		public final PathNode countKeys = leaf("countKeys");
		public final PathNode deleteData = leaf("deleteData");
		public final PathNode getData = leaf("getData");
		public final PathNode search = leaf("search");
	}

	public static class BrowseDataPaths extends PathNode{
		public final PathNode browseData = leaf("browseData");
		public final PathNode countKeys = leaf("countKeys");
	}

	public static class InfoPaths extends PathNode{
		public final PathNode clients = leaf("clients");
		public final PathNode listeners = leaf("listeners");
		public final PathNode routeSets = leaf("routeSets");
		public final PathNode filters = leaf("filters");
		public final PathNode nodes = leaf("nodes");
		public final PathNode plugins = leaf("plugins");
		public final PathNode properties = leaf("properties");
	}

	public static class DocumentationPaths extends PathNode{
		public final PathNode readmes = leaf("readmes");
		public final PathNode systemDocs = leaf("systemDocs");
	}

	public static class TestApiPaths extends PathNode{
		public final PathNode before = leaf("before");
		public final PathNode year = leaf("year");
		public final PathNode now = leaf("now");
		public final PathNode length = leaf("length");
		public final PathNode size = leaf("size");
		public final PathNode count = leaf("count");
		public final PathNode first = leaf("first");
		public final PathNode hi = leaf("hi");
		public final PathNode hello = leaf("hello");
		public final PathNode banana = leaf("banana");
		public final PathNode bananas = leaf("bananas");
		public final PathNode describe = leaf("describe");
		public final PathNode sumInBase = leaf("sumInBase");
		public final PathNode printPrimitiveIntArray = leaf("printPrimitiveIntArray");
		public final PathNode printIntegerObjectArray = leaf("printIntegerObjectArray");
		public final PathNode printPrimitiveIntArrayNoParamName = leaf("printPrimitiveIntArrayNoParamName");
		public final PathNode printComplicatedArrayParams = leaf("printComplicatedArrayParams");
		public final PathNode timeContains = leaf("timeContains");
	}

}
