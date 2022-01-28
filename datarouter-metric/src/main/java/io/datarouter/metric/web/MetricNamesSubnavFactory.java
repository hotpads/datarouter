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
package io.datarouter.metric.web;

import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.metric.config.DatarouterMetricPaths;
import io.datarouter.plugin.PluginInjector;
import io.datarouter.web.html.nav.Subnav;
import io.datarouter.web.html.nav.Subnav.Dropdown;
import io.datarouter.web.metriclinks.MetricLinkPage;

@Singleton
public class MetricNamesSubnavFactory{

	public static final String ID = "metric-link-subnav";

	@Inject
	private PluginInjector pluginInjector;
	@Inject
	private DatarouterMetricPaths paths;

	public Subnav build(String contextPath){
		Subnav subnav = new Subnav("Metric Links", "", ID);
		pluginInjector.scanInstances(MetricLinkPage.KEY)
				.collect(Collectors.groupingBy(MetricLinkPage::getCategory))
				.entrySet()
				.forEach(entry -> {
					Dropdown dropdown = new Dropdown(entry.getKey().getName());
					entry.getValue().forEach(page -> dropdown.addItem(page.getName(),
							contextPath
									+ paths.datarouter.metric.metricLinks.view.toSlashedString()
									+ "#" + page.getHtmlId()));
					subnav.add(dropdown);
		});
		subnav.add(other(contextPath));
		return subnav;
	}

	private Dropdown other(String contextPath){
		return new Dropdown("Other")
				.addItem("Registered Metric Names",
						contextPath + paths.datarouter.metric.metricLinks.registeredNames.toSlashedString())
				.addItem("Dashboards",
						contextPath + paths.datarouter.metric.metricLinks.metricDashboards.toSlashedString())
				.addItem("Misc Metric Links",
						contextPath + paths.datarouter.metric.metricLinks.miscMetricLinks.toSlashedString());
	}

}
