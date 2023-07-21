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
package io.datarouter.storage.config.properties;

import java.util.function.Supplier;

import io.datarouter.storage.config.ComputedPropertiesFinder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class EnvironmentDomain implements Supplier<String>{

	public static final String ENVIRONMENT_DOMAIN = "environmentDomain";

	private final String environmentDomain;

	@Inject
	public EnvironmentDomain(ComputedPropertiesFinder finder){
		this.environmentDomain = finder.findProperty(ENVIRONMENT_DOMAIN);
	}

	@Override
	public String get(){
		return environmentDomain;
	}

}
