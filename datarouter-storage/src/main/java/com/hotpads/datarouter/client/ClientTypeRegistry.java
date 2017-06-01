package com.hotpads.datarouter.client;

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

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.util.core.concurrent.Lazy;
import com.hotpads.util.core.lang.ClassTool;

@Singleton
public class ClientTypeRegistry{

	private static final String CLIENT_TYPE_CLASS_NAME_LOCATION = "META-INF/datarouter/clientTypes";

	private final Lazy<Map<String,ClientType>> clientTypesByName;

	@Inject
	public ClientTypeRegistry(DatarouterInjector injector){
		this.clientTypesByName = Lazy.of(() -> streamClientType()
				.map(ClientTypeRegistry::listLines)
				.flatMap(List::stream)
				.distinct()
				.map(ClassTool::forName)
				.map(clientTypeClass -> (Class<? extends ClientType>)clientTypeClass.asSubclass(ClientType.class))
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

	public ClientType create(String name){
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
