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
package io.datarouter.secret.service;

import javax.inject.Singleton;

public interface SecretNamespacer{

	static final String SHARED = "shared";

	String getAppNamespace();
	String getSharedNamespace();

	default String appNamespaced(String secretName){
		return getAppNamespace() + secretName;
	}

	default String sharedNamespaced(String secretName){
		return getSharedNamespace() + secretName;
	}

	@Singleton
	public static class EmptyNamespacer implements SecretNamespacer{

		@Override
		public String getAppNamespace(){
			return "";
		}

		@Override
		public String getSharedNamespace(){
			return SHARED + '/';
		}

	}

}
