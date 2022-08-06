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
package io.datarouter.serviceconfig.config;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.instrumentation.serviceconfig.ServiceConfigurationDto;
import io.datarouter.instrumentation.serviceconfig.ServiceConfigurationPublisher;
import io.datarouter.storage.config.DatarouterSubscribersSupplier;
import io.datarouter.storage.config.properties.AdminEmail;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.config.service.DomainFinder;
import io.datarouter.web.listener.DatarouterAppListener;
import io.datarouter.web.service.DocumentationNamesAndLinksSupplier;
import io.datarouter.web.service.ServiceDescriptionSupplier;

@Singleton
public class DatarouterServiceConfigurationAppListener implements DatarouterAppListener{

	@Inject
	private DatarouterServiceConfigurationSettings settings;
	@Inject
	private ServiceConfigurationPublisher serviceConfigurationPublisher;
	@Inject
	private DatarouterSubscribersSupplier subscribers;
	@Inject
	private ServiceName serviceName;
	@Inject
	private ServletContextSupplier servletContext;
	@Inject
	private DomainFinder domainFinder;
	@Inject
	private ServiceDescriptionSupplier serviceDescriptionSupplier;
	@Inject
	private DocumentationNamesAndLinksSupplier documentationNamesAndLinksSupplier;
	@Inject
	private AdminEmail adminEmail;

	@Override
	public void onStartUp(){
		if(!settings.publishServiceConfig.get()){
			return;
		}
		Set<String> admins = new HashSet<>();
		Optional.ofNullable(adminEmail.get()).ifPresent(admins::add);
		//TODO remove this
		admins.addAll(subscribers.get());
		ServiceConfigurationDto dto = new ServiceConfigurationDto(
				serviceName.get(),
				admins,
				subscribers.get(),
				serviceDescriptionSupplier.get(),
				domainFinder.getDomainPreferPublic(),
				servletContext.getContextName(),
				documentationNamesAndLinksSupplier.getReadmeDocs());
		try{
			serviceConfigurationPublisher.add(dto);
		}catch(Exception e){
			// hide failure on startup
		}
	}

}
