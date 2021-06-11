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
package io.datarouter.gcp.spanner.config;

import io.datarouter.gcp.spanner.config.SpannerProjectIdAndInstanceIdSupplier.NoOpSpannerProjectIdAndInstanceIdSupplier;
import io.datarouter.opencensus.DatarouterOpencensusAppListener;
import io.datarouter.web.config.BaseWebPlugin;

public class DatarouterGcpSpannerPlugin extends BaseWebPlugin{

	private final Class<? extends SpannerProjectIdAndInstanceIdSupplier> spannerProjectIdAndInstanceIdSupplier;

	private DatarouterGcpSpannerPlugin(
			Class<? extends SpannerProjectIdAndInstanceIdSupplier> spannerProjectIdAndInstanceIdSupplier){
		this.spannerProjectIdAndInstanceIdSupplier = spannerProjectIdAndInstanceIdSupplier;
		addDatarouterGithubDocLink("datarouter-gcp-spanner");
		addDynamicNavBarItem(GcpSpannerNavBarItem.class);
		addAppListener(DatarouterOpencensusAppListener.class);
	}

	@Override
	protected void configure(){
		bind(SpannerProjectIdAndInstanceIdSupplier.class).to(spannerProjectIdAndInstanceIdSupplier);
	}

	public static class DatarouterGcpSpannerPluginBuilder{

		private Class<? extends SpannerProjectIdAndInstanceIdSupplier> spannerProjectIdAndInstanceIdSupplier
				= NoOpSpannerProjectIdAndInstanceIdSupplier.class;

		public DatarouterGcpSpannerPluginBuilder setProjectIdAndInstanceId(
				Class<? extends SpannerProjectIdAndInstanceIdSupplier> spannerProjectIdAndInstanceIdSupplier){
			this.spannerProjectIdAndInstanceIdSupplier = spannerProjectIdAndInstanceIdSupplier;
			return this;
		}


		public DatarouterGcpSpannerPlugin build(){
			return new DatarouterGcpSpannerPlugin(spannerProjectIdAndInstanceIdSupplier);
		}

	}

}
