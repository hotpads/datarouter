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
package io.datarouter.filesystem.client;

import io.datarouter.storage.client.ClientType;
import io.datarouter.web.browse.DatarouterClientWebInspectorRegistry;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class FilesystemClientType implements ClientType<FilesystemClientNodeFactory,FilesystemClientManager>{

	protected static final String NAME = "filesystem";

	@Inject
	public FilesystemClientType(DatarouterClientWebInspectorRegistry datarouterClientWebInspectorRegistry){
		datarouterClientWebInspectorRegistry.register(NAME, FilesystemWebInspector.class);
	}

	@Override
	public String getName(){
		return NAME;
	}

	@Override
	public Class<FilesystemClientNodeFactory> getClientNodeFactoryClass(){
		return FilesystemClientNodeFactory.class;
	}

	@Override
	public Class<FilesystemClientManager> getClientManagerClass(){
		return FilesystemClientManager.class;
	}

}
