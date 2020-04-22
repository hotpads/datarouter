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
package io.datarouter.websocket.endpoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.util.singletonsupplier.SingletonSupplier;

public abstract class WebSocketServices{

	private final DatarouterInjector injector;

	private final List<Class<? extends WebSocketService>> services;
	private final SingletonSupplier<Map<String,WebSocketService>> serviceMap;

	public WebSocketServices(DatarouterInjector injector){
		this.injector = injector;
		this.services = new ArrayList<>();
		// lazy to avoid circular dependency
		this.serviceMap = SingletonSupplier.of(() -> {
			return services.stream()
					.map(injector::getInstance)
					.collect(Collectors.toMap(WebSocketService::getName, Function.identity()));
		});
	}

	protected void registerService(Class<? extends WebSocketService> clazz){
		services.add(clazz);
	}

	public WebSocketService getNewInstance(String serviceName){
		WebSocketService sampleService = serviceMap.get().get(serviceName);
		if(sampleService == null){
			return null;
		}
		// generate a new instance
		return injector.getInstance(sampleService.getClass());
	}

	public Collection<WebSocketService> listSampleInstances(){
		return serviceMap.get().values();
	}

}
