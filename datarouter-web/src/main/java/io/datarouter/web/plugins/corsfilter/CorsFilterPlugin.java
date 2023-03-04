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
package io.datarouter.web.plugins.corsfilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.dispatcher.FilterParamGrouping;
import io.datarouter.web.dispatcher.FilterParams;
import io.datarouter.web.inject.guice.BaseGuiceServletModule;

public class CorsFilterPlugin extends BaseWebPlugin{

	private final CorsOriginFilter params;

	private CorsFilterPlugin(CorsOriginFilter params){
		this.params = params;
		addFilterParams(new FilterParams(
				false,
				BaseGuiceServletModule.ROOT_PATH,
				CorsFilter.class,
				FilterParamGrouping.DATAROUTER));
	}

	@Override
	protected void configure(){
		bind(CorsOriginFilter.class).toInstance(params);
	}

	public static class CorsFilterPluginBuilder{

		private final List<Predicate<String>> originFilters = new ArrayList<>();

		public CorsFilterPluginBuilder allowLocalhost(){
			return allow(Pattern.compile("https?://localhost(:\\d+)?"));
		}

		public CorsFilterPluginBuilder allow(Collection<String> equals){
			equals.forEach(this::allow);
			return this;
		}

		public CorsFilterPluginBuilder allow(String equals){
			this.originFilters.add(equals::equals);
			return this;
		}

		public CorsFilterPluginBuilder allow(Pattern pattern){
			this.originFilters.add(pattern.asMatchPredicate());
			return this;
		}

		public CorsFilterPlugin build(){
			return new CorsFilterPlugin(new CorsOriginFilter(originFilters));
		}

	}

}
