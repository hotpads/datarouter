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
package io.datarouter.conveyor.trace;

import io.datarouter.trace.filter.GuiceTraceFilter;
import io.datarouter.trace.service.TraceUrlBuilder;
import io.datarouter.trace.service.TraceUrlBuilder.NoOpTraceUrlBuilder;
import io.datarouter.trace.settings.DatarouterTraceFilterSettingRoot;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.config.DatarouterServletGuiceModule;
import io.datarouter.web.dispatcher.FilterParamGrouping;
import io.datarouter.web.dispatcher.FilterParams;

public class DatarouterTracePlugin extends BaseWebPlugin{

	private final Class<? extends TraceUrlBuilder> traceUrlBuilder;

	private DatarouterTracePlugin(
			Class<? extends TraceUrlBuilder> traceUrlBuilder){
		this.traceUrlBuilder = traceUrlBuilder;

		addSettingRoot(DatarouterTraceFilterSettingRoot.class);
		addFilterParams(new FilterParams(false, DatarouterServletGuiceModule.ROOT_PATH, GuiceTraceFilter.class,
				FilterParamGrouping.DATAROUTER));
		addDatarouterGithubDocLink("datarouter-trace");
	}

	@Override
	public void configure(){
		bind(TraceUrlBuilder.class).to(traceUrlBuilder);
	}

	public static class DatarouterTracePluginBuilder{

		private Class<? extends TraceUrlBuilder> traceUrlBuilder = NoOpTraceUrlBuilder.class;

		public DatarouterTracePluginBuilder enableTracePublishing(
				Class<? extends TraceUrlBuilder> traceUrlBuilder){
			this.traceUrlBuilder = traceUrlBuilder;
			return this;
		}

		public DatarouterTracePlugin build(){
			return new DatarouterTracePlugin(
					traceUrlBuilder);
		}

	}

}
