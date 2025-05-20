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
package io.datarouter.relay;

import io.datarouter.inject.DatarouterInjector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * The main purpose of this class is to break a circular dependency related to cluster settings by injecting a Provider
 */
@Singleton
public class DatarouterRelaySenderProvider{

	@Inject
	private DatarouterInjector injector;

	public DatarouterRelaySender get(){
		return injector.getInstance(DatarouterRelaySender.class);
	}

}
