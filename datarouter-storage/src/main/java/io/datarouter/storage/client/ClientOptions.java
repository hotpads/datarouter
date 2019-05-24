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

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.Properties;
import java.util.function.BiFunction;

import javax.inject.Singleton;

import io.datarouter.util.properties.TypedProperties;

@Singleton
public class ClientOptions{

	private static final String CLIENT_default = "default";
	private static final String PREFIX_client = "client.";
	private static final String PARAM_initMode = "initMode";
	private static final String PARAM_type = "type";

	private final TypedProperties typedProperties = new TypedProperties();

	public void addProperties(Properties properties){
		typedProperties.addProperties(properties);
	}

	public ClientInitMode getInitMode(ClientId clientId, ClientInitMode def){
		return getClientPropertyOrDefault((key, defaultValue) -> ClientInitMode.fromString(typedProperties.getString(
				key), defaultValue), PARAM_initMode, clientId.getName(), def);
	}

	public String getClientType(ClientId clientId){
		return getRequiredString(clientId.getName(), PARAM_type);
	}

	public String getRequiredString(String clientName, String propertyKey){
		return typedProperties.getRequiredString(makeKey(clientName, propertyKey));
	}

	public Integer getRequiredInteger(String clientName, String propertyKey){
		return typedProperties.getRequiredInteger(makeKey(clientName, propertyKey));
	}

	public Optional<String> optString(String clientName, String propertyKey){
		return typedProperties.optString(makeKey(clientName, propertyKey));
	}

	public Optional<InetSocketAddress> optInetSocketAddress(String clientName, String propertyKey){
		return typedProperties.optInetSocketAddress(makeKey(clientName, propertyKey));
	}

	public String getStringClientPropertyOrDefault(String propertyKey, String clientName, String def){
		return getClientPropertyOrDefault(typedProperties::getString, propertyKey, clientName, def);
	}

	public Integer getIntegerClientPropertyOrDefault(String propertyKey, String clientName, Integer def){
		return getClientPropertyOrDefault(typedProperties::getInteger, propertyKey, clientName, def);
	}

	public Boolean getBooleanClientPropertyOrDefault(String propertyKey, String clientName, Boolean def){
		return getClientPropertyOrDefault(typedProperties::getBoolean, propertyKey, clientName, def);
	}

	private static String makeKey(String clientName, String propertyKey){
		return PREFIX_client + clientName + "." + propertyKey;
	}

	private static <T> T getClientPropertyOrDefault(BiFunction<String,T,T> propertyGetter, String propertyKey,
			String clientName, T def){
		T defaultValue = propertyGetter.apply(makeKey(CLIENT_default, propertyKey), def);
		return propertyGetter.apply(makeKey(clientName, propertyKey), defaultValue);
	}

}
