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
package io.datarouter.httpclient.client;

import io.datarouter.instrumentation.metric.node.BaseMetricRoot;
import io.datarouter.instrumentation.metric.node.MetricNode;

public class DatarouterHttpClientMetrics extends BaseMetricRoot{

	private static final DatarouterHttpClientMetrics ROOT = new DatarouterHttpClientMetrics();

	public static void incResponseStatusCode(String name, int statusCode){
		ROOT.name(name).response.statusCode(statusCode).count();
	}

	public static void incRequest(String name){
		ROOT.name(name).request.count();
	}

	public static void incIoException(String name){
		ROOT.name(name).iOException.count();
	}

	public static void incCancellationException(String name){
		ROOT.name(name).cancellationException.count();
	}

	public static void incSlowRequest(String name){
		ROOT.name(name).slowRequest.count();
	}

	public static void incTraceContextNull(String name){
		ROOT.name(name).traceContextNull.count();
	}

	public static void durationMs(String name, long durationMs){
		ROOT.name(name).durationMs.gauge(durationMs);
	}

	public static DatarouterHttpNamedClientMetrics clientName(String clientName){
		return ROOT.name(clientName);
	}

	//setup

	private DatarouterHttpClientMetrics(){
		super("httpClient");
	}

	private DatarouterHttpNamedClientMetrics name(String name){
		return variable(DatarouterHttpNamedClientMetrics::new, name);
	}

	public static class DatarouterHttpNamedClientMetrics extends MetricNodeVariable<DatarouterHttpNamedClientMetrics>{
		public DatarouterHttpNamedClientMetrics(){
			super("name", "Client name", DatarouterHttpNamedClientMetrics::new);
		}

		private final Response response = literal(Response::new, "response");
		private final MetricNode traceContextNull = literal("traceContext null");
		private final MetricNode request = literal("request");
		private final MetricNode slowRequest = literal("slow request");
		private final MetricNode iOException = literal("IOException");
		private final MetricNode cancellationException = literal("CancellationException");
		private final MetricNode durationMs = literal("durationMs");

		public final MetricNode available = literal("available");
		public final MetricNode pending = literal("pending");
		public final MetricNode leased = literal("leased");
		public final MetricNode max = literal("max");
	}

	private static class Response extends MetricNode{
		private StatusCode statusCode(int statusCode){
			return variable(StatusCode::new, Integer.toString(statusCode));
		}
	}

	private static class StatusCode extends MetricNodeVariable<StatusCode>{
		private StatusCode(){
			super("statusCode", "Status code", StatusCode::new);
		}
	}

}
