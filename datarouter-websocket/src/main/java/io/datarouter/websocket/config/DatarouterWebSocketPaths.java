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
package io.datarouter.websocket.config;

import javax.inject.Singleton;

import io.datarouter.pathnode.PathNode;
import io.datarouter.pathnode.PathsRoot;
import io.datarouter.websocket.service.WebSocketConfig;

@Singleton
public class DatarouterWebSocketPaths extends PathNode implements PathsRoot{

	public final DatarouterPaths datarouter = branch(DatarouterPaths::new, "datarouter");
	public final WebsocketCommandPaths websocketCommand = branch(WebsocketCommandPaths::new, "websocketCommand");
	public final WsPaths ws = branch(WsPaths::new, WebSocketConfig.WEBSOCKET_URI_PREFIX);

	public static class DatarouterPaths extends PathNode{
		public final WebsocketTool websocketTool = branch(WebsocketTool::new, "websocketTool");
	}

	public static class WebsocketCommandPaths extends PathNode{
		public final PathNode push = leaf("push");
		public final PathNode isAlive = leaf("isAlive");
	}

	public static class WsPaths extends PathNode{
		public final PathNode echo = leaf("echo");
		public final PathNode services = leaf("services");
	}

	public static class WebsocketTool extends PathNode{
		public final PathNode list = leaf("list");
		public final PathNode subscriptions = leaf("subscriptions");
	}

}
