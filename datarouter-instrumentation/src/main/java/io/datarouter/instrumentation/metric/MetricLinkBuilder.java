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
package io.datarouter.instrumentation.metric;

public interface MetricLinkBuilder{

	String availableMetricsLink(String prefix);
	String exactMetricLink(String name);
	String exactMetricLink(String name, Long startTime, Long endTime);
	String dashboardLink(String id);
	String serviceCostLink();

	static class NoOpMetricLinkBuilder implements MetricLinkBuilder{

		@Override
		public String availableMetricsLink(String prefix){
			return "";
		}

		@Override
		public String exactMetricLink(String name){
			return "";
		}

		@Override
		public String exactMetricLink(String name, Long startTime, Long endTime){
			return "";
		}

		@Override
		public String dashboardLink(String id){
			return "";
		}

		@Override
		public String serviceCostLink(){
			return "";
		}

	}

}
