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
package io.datarouter.secretweb.service;

import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.secret.service.SecretNamespacer;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.config.environment.EnvironmentType;

@Singleton
public class DatarouterPropertiesAndServiceSecretNamespacer implements SecretNamespacer{

	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private DatarouterService datarouterService;

	private String getEnvironment(){
		return Objects.requireNonNull(datarouterProperties.getEnvironmentType());
	}

	@Override
	public String getAppNamespace(){
		return getEnvironment() + '/' + Objects.requireNonNull(datarouterService.getServiceName()) + '/';
	}

	@Override
	public String getSharedNamespace(){
		return getEnvironment() + '/' + SHARED + '/';
	}

	@Override
	public boolean isDevelopment(){
		return EnvironmentType.DEVELOPMENT.get().getPersistentString().equals(getEnvironment());
	}

}
