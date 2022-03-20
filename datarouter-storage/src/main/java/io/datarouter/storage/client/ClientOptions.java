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
package io.datarouter.storage.client;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import io.datarouter.util.properties.TypedProperties;

@Singleton
public class ClientOptions{

	private static final String PREFIX_client = "client.";
	private static final String PARAM_initMode = "initMode";
	private static final String PARAM_type = "type";
	private static final String CLIENT_default = ClientOptionsBuilder.DEFAULT_CLIENT_ID.getName();

	private final TypedProperties typedProperties = new TypedProperties();

	public void addProperties(Properties properties){
		typedProperties.addProperties(properties);
	}

	public ClientInitMode getInitMode(ClientId clientId, ClientInitMode def){
		return getClientPropertyOrDefault(
				(key, defaultValue) -> ClientInitMode.fromString(typedProperties.getString(key), defaultValue),
				PARAM_initMode,
				clientId.getName(),
				def);
	}

	public boolean isClientTypePresent(ClientId clientId){
		return optString(clientId.getName(), PARAM_type).isPresent();
	}

	public String getClientType(ClientId clientId){
		return getRequiredString(clientId.getName(), PARAM_type);
	}

	public String getRequiredString(String clientName, String propertyKey){
		return typedProperties.getRequiredString(makeClientPrefixedKey(clientName, propertyKey));
	}

	public Integer getRequiredInteger(String clientName, String propertyKey){
		return typedProperties.getRequiredInteger(makeClientPrefixedKey(clientName, propertyKey));
	}

	public Optional<String> optString(String clientName, String propertyKey){
		return typedProperties.optString(makeClientPrefixedKey(clientName, propertyKey));
	}

	public Optional<InetSocketAddress> optInetSocketAddress(String clientName, String propertyKey){
		return typedProperties.optInetSocketAddress(makeClientPrefixedKey(clientName, propertyKey));
	}

	public String getStringClientPropertyOrDefault(String propertyKey, String clientName, String defaultValue){
		return getClientPropertyOrDefault(typedProperties::getString, propertyKey, clientName, defaultValue);
	}

	public Integer getIntegerClientPropertyOrDefault(String propertyKey, String clientName, Integer defaultValue){
		return getClientPropertyOrDefault(typedProperties::getInteger, propertyKey, clientName, defaultValue);
	}

	public Boolean getBooleanClientPropertyOrDefault(String propertyKey, String clientName, Boolean defaultValue){
		return getClientPropertyOrDefault(typedProperties::getBoolean, propertyKey, clientName, defaultValue);
	}

	public Map<String,String> getAllClientOptions(String clientName){
		String clientPrefixedName = PREFIX_client + clientName + ".";
		Map<String,String> allClientOptions = typedProperties.getUnmodifiablePropertiesList().stream()
				.flatMap(properties -> properties.entrySet().stream())
				.filter(entry -> entry.getKey().toString().startsWith(clientPrefixedName))
				.collect(Collectors.toMap(
						entry -> entry.getKey().toString().replace(clientPrefixedName, ""),
						entry -> entry.getValue().toString()));
		ClientInitMode initMode = getClientPropertyOrDefault(
				(key, defaultValue) -> ClientInitMode.fromString(typedProperties.getString(key), defaultValue),
				PARAM_initMode,
				clientName,
				ClientInitMode.lazy);
		allClientOptions.put(PARAM_initMode, initMode.name());
		return allClientOptions;
	}

	public static String makeClientTypeKey(String clientName){
		return makeClientPrefixedKey(clientName, PARAM_type);
	}

	public static String makeClientInitModeKey(String clientName){
		return makeClientPrefixedKey(clientName, PARAM_initMode);
	}

	public static String makeClientPrefixedKey(String clientName, String propertyKey){
		return PREFIX_client + clientName + "." + propertyKey;
	}

	private static <T> T getClientPropertyOrDefault(
			BiFunction<String,T,T> propertyGetter,
			String propertyKey,
			String clientName,
			T defaultValue){
		T defaultAltValue = propertyGetter.apply(makeClientPrefixedKey(CLIENT_default, propertyKey), defaultValue);
		return propertyGetter.apply(makeClientPrefixedKey(clientName, propertyKey), defaultAltValue);
	}

}
