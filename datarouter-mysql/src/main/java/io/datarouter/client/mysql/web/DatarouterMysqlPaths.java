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
package io.datarouter.client.mysql.web;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.pathnode.PathNode;
import io.datarouter.pathnode.PathsRoot;
import io.datarouter.web.config.DatarouterWebPaths;

@Singleton
public class DatarouterMysqlPaths extends PathNode implements PathsRoot{

	public final DatarouterPaths datarouter;

	@Inject
	public DatarouterMysqlPaths(DatarouterWebPaths datarouterWebPaths){
		this.datarouter = branch(DatarouterPaths::new, datarouterWebPaths.datarouter.getValue());
	}

	public static class DatarouterPaths extends PathNode{
		public final ClientsPaths clients = branch(ClientsPaths::new, "cilents");
	}

	public static class ClientsPaths extends PathNode{
		public final PathNode mysql = leaf("mysql");
	}

}
