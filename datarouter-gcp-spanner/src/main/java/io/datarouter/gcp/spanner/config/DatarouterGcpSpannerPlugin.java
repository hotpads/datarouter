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
package io.datarouter.gcp.spanner.config;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.gcp.spanner.config.SpannerProjectIdAndInstanceIdSupplier.NoOpSpannerProjectIdAndInstanceIdSupplier;
import io.datarouter.gcp.spanner.field.SpannerBaseFieldCodec;
import io.datarouter.gcp.spanner.field.SpannerFieldCodecRegistry;
import io.datarouter.gcp.spanner.field.SpannerFieldCodecs;
import io.datarouter.model.field.Field;
import io.datarouter.opencensus.DatarouterOpencensusAppListener;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.plugins.opencencus.metrics.OpencencusMetricsMapper;

public class DatarouterGcpSpannerPlugin extends BaseWebPlugin{

	private final Class<? extends SpannerProjectIdAndInstanceIdSupplier> spannerProjectIdAndInstanceIdSupplier;
	private final List<SpannerFieldCodec> fieldCodecs;

	private DatarouterGcpSpannerPlugin(
			Class<? extends SpannerProjectIdAndInstanceIdSupplier> spannerProjectIdAndInstanceIdSupplier,
			List<SpannerFieldCodec> fieldCodecs){
		this.spannerProjectIdAndInstanceIdSupplier = spannerProjectIdAndInstanceIdSupplier;
		this.fieldCodecs = fieldCodecs;
		addDatarouterGithubDocLink("datarouter-gcp-spanner");
		addDynamicNavBarItem(GcpSpannerNavBarItem.class);
		addAppListener(DatarouterOpencensusAppListener.class);
		addPluginEntry(OpencencusMetricsMapper.KEY, GcpSpannerOpencencusMetrics.class);
//		DatarouterSpannerLog4j2Configuration.assertHasRun();
	}

	@Override
	protected void configure(){
		bind(SpannerProjectIdAndInstanceIdSupplier.class).to(spannerProjectIdAndInstanceIdSupplier);
		var fieldCodecRegistry = new SpannerFieldCodecRegistry();
		fieldCodecs.forEach(dto -> fieldCodecRegistry.addCodec(dto.fieldClass(), dto.spannerCodec()));
		bind(SpannerFieldCodecs.class).toInstance(fieldCodecRegistry);
	}

	public static class DatarouterGcpSpannerPluginBuilder{

		private Class<? extends SpannerProjectIdAndInstanceIdSupplier> spannerProjectIdAndInstanceIdSupplier
				= NoOpSpannerProjectIdAndInstanceIdSupplier.class;
		private final List<SpannerFieldCodec> fieldCodecs = new ArrayList<>();

		public DatarouterGcpSpannerPluginBuilder setProjectIdAndInstanceId(
				Class<? extends SpannerProjectIdAndInstanceIdSupplier> spannerProjectIdAndInstanceIdSupplier){
			this.spannerProjectIdAndInstanceIdSupplier = spannerProjectIdAndInstanceIdSupplier;
			return this;
		}

		public <F extends Field<?>,
				C extends SpannerBaseFieldCodec<?,?>>
		DatarouterGcpSpannerPluginBuilder addFieldCodec(
				Class<F> fieldClass,
				Class<C> codecClass){
			fieldCodecs.add(new SpannerFieldCodec(fieldClass, codecClass));
			return this;
		}

		public DatarouterGcpSpannerPlugin build(){
			return new DatarouterGcpSpannerPlugin(spannerProjectIdAndInstanceIdSupplier, fieldCodecs);
		}

	}

}
