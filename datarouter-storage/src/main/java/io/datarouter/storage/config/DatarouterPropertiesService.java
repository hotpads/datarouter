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
package io.datarouter.storage.config;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.properties.AdminEmail;
import io.datarouter.storage.config.properties.ConfigDirectory;
import io.datarouter.storage.config.properties.DatarouterEnvironmentTypeSupplier;
import io.datarouter.storage.config.properties.DatarouterServerTypeSupplier;
import io.datarouter.storage.config.properties.EnvironmentDomain;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.config.properties.InternalConfigDirectory;
import io.datarouter.storage.config.properties.ServerClusterDomains;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.config.properties.ServerPrivateIp;
import io.datarouter.storage.config.properties.ServerPublicIp;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.util.Require;

@Singleton
public class DatarouterPropertiesService{

	@Inject
	private AdminEmail adminEmail;
	@Inject
	private EnvironmentName environmentName;
	@Inject
	private EnvironmentDomain environmentDomain;
	@Inject
	private DatarouterEnvironmentTypeSupplier environmentType;
	@Inject
	private ServiceName serviceName;
	@Inject
	private InternalConfigDirectory internalConfigDirectory;
	@Inject
	private ServerClusterDomains serverClusterDomains;
	@Inject
	private ServerName serverName;
	@Inject
	private ServerPrivateIp serverPrivateIp;
	@Inject
	private ServerPublicIp serverPublicIp;
	@Inject
	private DatarouterServerTypeSupplier serverType;
	@Inject
	private DatarouterSubscribersSupplier subscribers;
	@Inject
	private ConfigDirectory configDirectory;

	public List<DatarouterProperty> getAllProperties(){
		return Scanner.of(
				new DatarouterProperty(AdminEmail.ADMINISTRATOR_EMAIL, adminEmail.get()),
				new DatarouterProperty(EnvironmentName.ENVIRONMENT, environmentName.get()),
				new DatarouterProperty(EnvironmentDomain.ENVIRONMENT_DOMAIN, environmentDomain.get()),
				new DatarouterProperty(DatarouterEnvironmentTypeSupplier.ENVIRONMENT_TYPE, environmentType.get()),
				new DatarouterProperty(ServiceName.ENV_VARIABLE, serviceName.get()),
				new DatarouterProperty(InternalConfigDirectory.INTERNAL_CONFIG_DIRECTORY,
						internalConfigDirectory.get()),
				new DatarouterProperty(ServerClusterDomains.SERVER_CLUSTER_DOMAINS, serverClusterDomains.get().stream()
						.sorted()
						.collect(Collectors.joining(","))),
				new DatarouterProperty(ServerName.SERVER_NAME, serverName.get()),
				new DatarouterProperty(ServerPrivateIp.SERVER_PRIVATE_IP, serverPrivateIp.get()),
				new DatarouterProperty(ServerPublicIp.SERVER_PUBLIC_IP, serverPublicIp.get()),
				new DatarouterProperty(DatarouterServerTypeSupplier.SERVER_TYPE,
						serverType.get().getPersistentString()),
				new DatarouterProperty("subscribers", subscribers.get().stream()
						.sorted()
						.collect(Collectors.joining(","))),
				new DatarouterProperty("configDirectory", configDirectory.get()))
				.sort(Comparator.comparing(DatarouterProperty::key))
				.list();
	}

	public record DatarouterProperty(
			String key,
			String value){
	}

	public void assertRequired(){
		Require.notNull(adminEmail.get());
		Require.notNull(environmentName.get());
		Require.notNull(environmentType.get());
		Require.notNull(serverName.get());
		Require.notNull(serverType.get());
		Require.notNull(internalConfigDirectory.get());
	}

}
