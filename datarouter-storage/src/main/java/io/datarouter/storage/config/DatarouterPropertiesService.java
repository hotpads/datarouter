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
import io.datarouter.util.Require;
import io.datarouter.util.tuple.Twin;

@Singleton
public class DatarouterPropertiesService{

	@Inject
	private AdminEmail adminEmail;
	@Inject
	private EnvironmentName enviornmenteName;
	@Inject
	private EnvironmentDomain environmentDomain;
	@Inject
	private DatarouterEnvironmentTypeSupplier environmentType;
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

	public List<Twin<String>> getAllProperties(){
		return Scanner.of(
				Twin.of(AdminEmail.ADMINISTRATOR_EMAIL, adminEmail.get()),
				Twin.of(EnvironmentName.ENVIRONMENT, enviornmenteName.get()),
				Twin.of(EnvironmentDomain.ENVIRONMENT_DOMAIN, environmentDomain.get()),
				Twin.of(DatarouterEnvironmentTypeSupplier.ENVIRONMENT_TYPE, environmentType.get()),
				Twin.of(InternalConfigDirectory.INTERNAL_CONFIG_DIRECTORY, internalConfigDirectory.get()),
				Twin.of(ServerClusterDomains.SERVER_CLUSTER_DOMAINS, serverClusterDomains.get().stream()
						.sorted()
						.collect(Collectors.joining(","))),
				Twin.of(ServerName.SERVER_NAME, serverName.get()),
				Twin.of(ServerPrivateIp.SERVER_PRIVATE_IP, serverPrivateIp.get()),
				Twin.of(ServerPublicIp.SERVER_PUBLIC_IP, serverPublicIp.get()),
				Twin.of(DatarouterServerTypeSupplier.SERVER_TYPE, serverType.get().getPersistentString()),
				Twin.of("subscribers", subscribers.get().stream()
						.sorted()
						.collect(Collectors.joining(","))),
				Twin.of("configDirectory", configDirectory.get()))
				.sort(Comparator.comparing(Twin::getLeft))
				.list();
	}

	public void assertRequired(){
		Require.notNull(adminEmail.get());
		Require.notNull(enviornmenteName.get());
		Require.notNull(environmentType.get());
		Require.notNull(serverName.get());
		Require.notNull(serverType.get());
		Require.notNull(internalConfigDirectory.get());
	}

}
