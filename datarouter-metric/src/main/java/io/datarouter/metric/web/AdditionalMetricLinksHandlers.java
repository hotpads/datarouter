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
package io.datarouter.metric.web;

import javax.inject.Inject;

import io.datarouter.metric.links.MetricNameRegistry;
import io.datarouter.metric.service.ViewMetricNameService;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import j2html.TagCreator;

public class AdditionalMetricLinksHandlers{

	private static final String METRIC_LINKS = "Metric Links - ";

	public static class RegisteredMetricNames extends BaseHandler{

		@Inject
		private MetricLinkPageFactory pageFactory;
		@Inject
		private ViewMetricNameService service;
		@Inject
		private MetricNameRegistry registry;

		@Handler(defaultHandler = true)
		public Mav view(){
			String title = "Registered Names";
			return pageFactory.startBuilder(request)
					.withTitle(METRIC_LINKS + title)
					.withContent(service.makeMetricNameTable(title, registry.metricNames))
					.buildMav();
		}

	}

	public static class MetricDashboardHandler extends BaseHandler{

		@Inject
		private MetricLinkPageFactory pageFactory;
		@Inject
		private ViewMetricNameService service;
		@Inject
		private ServerTypeDetector serverTypeDetector;

		@Handler(defaultHandler = true)
		public Mav view(){
			String title = "Dashboards";
			var content = service.getDashboardsTable();
			if(!serverTypeDetector.mightBeProduction()){
				// non production server types won't have the same dashboard ids as production
				content = TagCreator.div();
			}
			return pageFactory.startBuilder(request)
					.withTitle(METRIC_LINKS + title)
					.withContent(content)
					.buildMav();
		}

	}

	public static class MiscMetricLinksHandler extends BaseHandler{

		@Inject
		private MetricLinkPageFactory pageFactory;
		@Inject
		private ViewMetricNameService service;

		@Handler(defaultHandler = true)
		public Mav view(){
			String title = "Misc Metric Links";
			var content = service.miscMetricLinksTable();
			return pageFactory.startBuilder(request)
					.withTitle(METRIC_LINKS + title)
					.withContent(content)
					.buildMav();
		}

	}

}
