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
package io.datarouter.storage.config.properties;

import java.util.Optional;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.config.ComputedPropertiesFinder;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.storage.servertype.ServerTypes;

@Singleton
public class DatarouterServerTypeSupplier implements Supplier<ServerType>{

	public static final String SERVER_TYPE = "server.type";

	private final ServerType serverType;

	@Inject
	public DatarouterServerTypeSupplier(ComputedPropertiesFinder finder, ServerTypes serverTypes){
		this.serverType = serverTypes.fromPersistentString(finder.findProperty(SERVER_TYPE));
	}

	@Override
	public ServerType get(){
		return serverType;
	}

	public String getServerTypeString(){
		return Optional.ofNullable(serverType)
				.map(ServerType::getPersistentString)
				.orElse(null);
	}

}
