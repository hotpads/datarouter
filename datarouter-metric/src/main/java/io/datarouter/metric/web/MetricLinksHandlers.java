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

import io.datarouter.metric.MetricNameRegistry;
import io.datarouter.metric.service.ViewMetricNameService;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import j2html.TagCreator;

public class MetricLinksHandlers{

	private static final String METRIC_LINKS = "Metric Links - ";

	public static class MetricNamesAppHandler extends BaseHandler{

		@Inject
		private MetricNamesPageFactory pageFactory;
		@Inject
		private ViewMetricNameService service;

		@Handler(defaultHandler = true)
		public Mav view(){
			String title = "App Handlers";
			return pageFactory.startBuilder(request)
					.withTitle(METRIC_LINKS + title)
					.withContent(service.getHandlerMetricNames(title, false))
					.buildMav();
		}

	}

	public static class MetricNamesDatarouterHandler extends BaseHandler{

		@Inject
		private MetricNamesPageFactory pageFactory;
		@Inject
		private ViewMetricNameService service;

		@Handler(defaultHandler = true)
		public Mav view(){
			String title = "Datarouter Handlers";
			return pageFactory.startBuilder(request)
					.withTitle(METRIC_LINKS + title)
					.withContent(service.getHandlerMetricNames(title, true))
					.buildMav();
		}

	}

	public static class MetricNamesAppJobs extends BaseHandler{

		@Inject
		private MetricNamesPageFactory pageFactory;
		@Inject
		private ViewMetricNameService service;

		@Handler(defaultHandler = true)
		public Mav view(){
			String title = "App Jobs";
			return pageFactory.startBuilder(request)
					.withTitle(METRIC_LINKS + title)
					.withContent(service.makeMetricNameTable(title, service.getJobMetricNames(false)))
					.buildMav();
		}

	}

	public static class MetricNamesDatarouterJobs extends BaseHandler{

		@Inject
		private MetricNamesPageFactory pageFactory;
		@Inject
		private ViewMetricNameService service;

		@Handler(defaultHandler = true)
		public Mav view(){
			String title = "Datarouter Jobs";
			return pageFactory.startBuilder(request)
					.withTitle(METRIC_LINKS + title)
					.withContent(service.makeMetricNameTable(title, service.getJobMetricNames(true)))
					.buildMav();
		}

	}

	public static class MetricNamesAppTables extends BaseHandler{

		@Inject
		private MetricNamesPageFactory pageFactory;
		@Inject
		private ViewMetricNameService service;

		@Handler(defaultHandler = true)
		public Mav view(){
			String title = "App Tables";
			return pageFactory.startBuilder(request)
					.withTitle(METRIC_LINKS + title)
					.withContent(service.makeMetricNameTable(title, service.getNodeTableMetricNames(false)))
					.buildMav();
		}

	}

	public static class MetricNamesDatarouterTables extends BaseHandler{

		@Inject
		private MetricNamesPageFactory pageFactory;
		@Inject
		private ViewMetricNameService service;

		@Handler(defaultHandler = true)
		public Mav view(){
			String title = "Datarouter Tables";
			return pageFactory.startBuilder(request)
					.withTitle(METRIC_LINKS + title)
					.withContent(service.makeMetricNameTable(title, service.getNodeTableMetricNames(true)))
					.buildMav();
		}

	}

	public static class RegisteredMetricNames extends BaseHandler{

		@Inject
		private MetricNamesPageFactory pageFactory;
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
		private MetricNamesPageFactory pageFactory;
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

}
