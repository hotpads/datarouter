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
package io.datarouter.secret.service;

import io.datarouter.secret.op.SecretOp;
import io.datarouter.secret.op.SecretOpConfig;
import io.datarouter.secret.op.client.SecretClientOp;
import jakarta.inject.Singleton;

/**
 * Determines the appropriate namespace to use as a prefix to a secret's name in the currently running service. The two
 * built in namespaces are intended to allow each service (app) to either share or keep private the namespaced secret.
 * Namespacing is automatic and namespace privacy is respected by {@link SecretOp} and {@link SecretService} (except for
 * manual namespacing); but {@link SecretClientOp}s are basic and do not have namespacing built in.
 *
 * NOTE: Securing secrets based on names and namepaces is out of scope for datarouter. It is determined by the
 * secret access controls of the provider service.
 */
public interface SecretNamespacer{

	static final String SHARED = "shared";

	String getAppNamespace();
	String getSharedNamespace();
	boolean isDevelopment();

	default String appNamespaced(String secretName){
		return getAppNamespace() + secretName;
	}

	default String sharedNamespaced(String secretName){
		return getSharedNamespace() + secretName;
	}

	default String getConfigNamespace(SecretOpConfig config){
		switch(config.namespaceType){
		case APP:
			return getAppNamespace();
		case SHARED:
			return getSharedNamespace();
		case MANUAL:
			return config.manualNamespace;
		default:
			throw new RuntimeException();
		}
	}

	@Singleton
	public static class DevelopmentNamespacer implements SecretNamespacer{

		private static final String DEVELOPMENT = "development";

		@Override
		public String getAppNamespace(){
			return DEVELOPMENT + "/";
		}

		@Override
		public String getSharedNamespace(){
			return getAppNamespace() + SHARED + '/';
		}

		@Override
		public boolean isDevelopment(){
			return true;
		}

	}

}
