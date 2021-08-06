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
package io.datarouter.web.browse;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.inject.DatarouterInjector;

@Singleton
public class DatarouterClientWebInspectorRegistry{

	private final Map<String,Class<? extends DatarouterClientWebInspector>> inspectorsByClientTypeName;
	private final DatarouterInjector injector;

	@Inject
	public DatarouterClientWebInspectorRegistry(DatarouterInjector injector){
		this.injector = injector;
		this.inspectorsByClientTypeName = new ConcurrentHashMap<>();
	}

	public void register(String clientTypeName, Class<? extends DatarouterClientWebInspector> inspector){
		inspectorsByClientTypeName.put(clientTypeName, inspector);
	}

	public Optional<DatarouterClientWebInspector> get(String clientTypeName){
		return Optional.ofNullable(inspectorsByClientTypeName.get(clientTypeName))
				.map(injector::getInstance);
	}

}
