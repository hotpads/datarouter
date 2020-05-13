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
package io.datarouter.serviceconfig.config;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.instrumentation.serviceconfig.ServiceConfigurationDto;
import io.datarouter.instrumentation.serviceconfig.ServiceConfigurationPublisher;
import io.datarouter.storage.config.DatarouterAdditionalAdministratorsSupplier;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.web.listener.DatarouterAppListener;
import io.datarouter.web.service.ServiceDescriptionSupplier;
import io.datarouter.web.service.ServiceDocumentationNamesAndLinksSupplier;

@Singleton
public class DatarouterServiceConfigurationAppListener implements DatarouterAppListener{

	@Inject
	private DatarouterServiceConfigurationSettings settings;
	@Inject
	private ServiceConfigurationPublisher serviceConfigurationPublisher;
	@Inject
	private DatarouterAdditionalAdministratorsSupplier additionalAdministrators;
	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private DatarouterService datarouterService;
	@Inject
	private ServiceDescriptionSupplier serviceDescriptionSupplier;
	@Inject
	private ServiceDocumentationNamesAndLinksSupplier serviceReadmeLinkSupplier;

	@Override
	public void onStartUp(){
		if(!settings.sendServiceConfigurationsToPontoon.get()){
			return;
		}
		Set<String> admins = new HashSet<>();
		Optional.ofNullable(datarouterProperties.getAdministratorEmail()).ifPresent(admins::add);
		admins.addAll(additionalAdministrators.get());
		ServiceConfigurationDto dto = new ServiceConfigurationDto(
				datarouterService.getName(),
				admins,
				serviceDescriptionSupplier.get(),
				serviceReadmeLinkSupplier.get());
		try{
			serviceConfigurationPublisher.add(dto);
		}catch(Exception e){
			// hide failure on startup
		}
	}

}
