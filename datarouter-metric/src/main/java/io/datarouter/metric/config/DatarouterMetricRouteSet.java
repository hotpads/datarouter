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
package io.datarouter.metric.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.metric.web.AdditionalMetricLinksHandlers.MetricDashboardHandler;
import io.datarouter.metric.web.AdditionalMetricLinksHandlers.MiscMetricLinksHandler;
import io.datarouter.metric.web.AdditionalMetricLinksHandlers.RegisteredMetricNames;
import io.datarouter.metric.web.MetricLinksHandler;
import io.datarouter.storage.tag.Tag;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.user.role.DatarouterUserRole;

@Singleton
public class DatarouterMetricRouteSet extends BaseRouteSet{

	@Inject
	public DatarouterMetricRouteSet(DatarouterMetricPaths paths){
		super(paths.datarouter);
		handle(paths.datarouter.metric.metricLinks.registeredNames).withHandler(RegisteredMetricNames.class);
		handle(paths.datarouter.metric.metricLinks.metricDashboards).withHandler(MetricDashboardHandler.class);
		handle(paths.datarouter.metric.metricLinks.miscMetricLinks).withHandler(MiscMetricLinksHandler.class);

		handle(paths.datarouter.metric.metricLinks.view).withHandler(MetricLinksHandler.class);
	}

	@Override
	protected DispatchRule applyDefault(DispatchRule rule){
		return rule
				.allowRoles(DatarouterUserRole.DATAROUTER_ADMIN, DatarouterUserRole.USER)
				.withTag(Tag.DATAROUTER);
	}

}
