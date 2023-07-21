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
package io.datarouter.opencensus;

import io.datarouter.opencensus.adapter.DatarouterOpencensusTraceExporter;
import io.datarouter.web.listener.DatarouterAppListener;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.samplers.Samplers;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterOpencensusAppListener implements DatarouterAppListener{

	@Inject
	private DatarouterOpencensusTraceExporter datarouterOpencensusTraceExporter;

	@Override
	public void onStartUp(){
		Tracing.getExportComponent().getSpanExporter().registerHandler(DatarouterOpencensusTraceExporter.class
				.getName(), datarouterOpencensusTraceExporter);
		TraceConfig traceConfig = Tracing.getTraceConfig();
		traceConfig.updateActiveTraceParams(traceConfig.getActiveTraceParams().toBuilder()
				.setSampler(Samplers.alwaysSample())
				.build());
	}

	@Override
	public void onShutDown(){
		Tracing.getExportComponent().shutdown();
	}

}
