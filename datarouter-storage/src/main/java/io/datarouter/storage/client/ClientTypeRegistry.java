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
package io.datarouter.storage.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.util.lang.ClassTool;
import io.datarouter.util.lazy.Lazy;

@Singleton
public class ClientTypeRegistry{

	private static final String CLIENT_TYPE_CLASS_NAME_LOCATION = "META-INF/datarouter/clientTypes";

	private final Lazy<Map<String,ClientType<?,?>>> clientTypesByName;

	@SuppressWarnings("unchecked")
	@Inject
	public ClientTypeRegistry(DatarouterInjector injector){
		this.clientTypesByName = Lazy.of(() -> streamClientType()
				.map(ClientTypeRegistry::listLines)
				.flatMap(List::stream)
				.distinct()
				.map(ClassTool::forName)
				.map(clientTypeClass -> (Class<? extends ClientType<?,?>>)clientTypeClass.asSubclass(ClientType.class))
				.map(injector::getInstance)
				.collect(Collectors.toMap(ClientType::getName, Function.identity())));
	}

	private static Stream<URL> streamClientType(){
		try{
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			return Collections.list(classLoader.getResources(CLIENT_TYPE_CLASS_NAME_LOCATION)).stream();
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

	public ClientType<?,?> get(String name){
		return clientTypesByName.get().get(name);
	}

	private static List<String> listLines(URL url){
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))){
			return reader.lines().collect(Collectors.toList());
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

}
