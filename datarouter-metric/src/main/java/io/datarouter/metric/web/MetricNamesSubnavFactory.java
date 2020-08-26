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
import javax.inject.Singleton;

import io.datarouter.metric.config.DatarouterMetricPaths;
import io.datarouter.web.html.nav.Subnav;
import io.datarouter.web.html.nav.Subnav.Dropdown;

@Singleton
public class MetricNamesSubnavFactory{

	@Inject
	private DatarouterMetricPaths paths;

	public Subnav build(String contextPath){
		return new Subnav("Metric Links", "")
				.add(app(contextPath))
				.add(datarouter(contextPath))
				.add(other(contextPath));
	}

	private Dropdown app(String contextPath){
		return new Dropdown("App")
				.addItem("Handlers", contextPath + paths.datarouter.metric.metricNames.appHandlers.toSlashedString())
				.addItem("Jobs", contextPath + paths.datarouter.metric.metricNames.appJobs.toSlashedString())
				.addItem("Tables", contextPath + paths.datarouter.metric.metricNames.appTables.toSlashedString());
	}

	private Dropdown datarouter(String contextPath){
		return new Dropdown("Datarouter")
				.addItem("Handlers",
						contextPath + paths.datarouter.metric.metricNames.datarouterHandlers.toSlashedString())
				.addItem("Jobs", contextPath + paths.datarouter.metric.metricNames.datarouterJobs.toSlashedString())
				.addItem("Tables",
						contextPath + paths.datarouter.metric.metricNames.datarouterTables.toSlashedString());
	}

	private Dropdown other(String contextPath){
		return new Dropdown("Other")
				.addItem("Registered Metric Names",
						contextPath + paths.datarouter.metric.metricNames.registeredNames.toSlashedString())
				.addItem("Dashboards",
						contextPath + paths.datarouter.metric.metricNames.metricDashboards.toSlashedString());
	}

}
