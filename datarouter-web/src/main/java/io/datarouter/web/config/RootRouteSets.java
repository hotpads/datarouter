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
package io.datarouter.web.config;

import java.util.List;

import javax.inject.Singleton;

import io.datarouter.web.dispatcher.BaseRouteSet;

@Singleton
public class RootRouteSets implements RootRouteSetsSupplier{

	private final List<BaseRouteSet> routeSets;

	public RootRouteSets(List<BaseRouteSet> routeSets){
		this.routeSets = routeSets;
	}

	@Override
	public List<BaseRouteSet> get(){
		return routeSets;
	}

}
