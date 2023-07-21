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

import java.util.ArrayList;
import java.util.List;

import io.datarouter.conveyor.ConveyorConfigurationGroup;
import io.datarouter.instrumentation.gauge.GaugePublisher;
import io.datarouter.instrumentation.gauge.GaugePublisher.NoOpGaugePublisher;
import io.datarouter.instrumentation.metric.MetricLinkBuilder;
import io.datarouter.instrumentation.metric.MetricLinkBuilder.NoOpMetricLinkBuilder;
import io.datarouter.metric.counter.CountQueueDao;
import io.datarouter.metric.counter.CountQueueDao.DatarouterCountQueueDaoParams;
import io.datarouter.metric.counter.CountersAppListener;
import io.datarouter.metric.counter.collection.CountPublisher;
import io.datarouter.metric.counter.collection.CountPublisher.NoOpCountPublisher;
import io.datarouter.metric.counter.conveyor.CountConveyorConfigurationGroup;
import io.datarouter.metric.dto.MetricDashboardDto;
import io.datarouter.metric.dto.MetricName;
import io.datarouter.metric.dto.MiscMetricLinksDto;
import io.datarouter.metric.gauge.DatabeanGauges;
import io.datarouter.metric.gauge.GaugeQueueDao;
import io.datarouter.metric.gauge.GaugeQueueDao.GaugeQueueDaoParams;
import io.datarouter.metric.gauge.conveyor.GaugeConveyorConfigurationGroup;
import io.datarouter.metric.links.MetricDashboardRegistry;
import io.datarouter.metric.links.MetricNameRegistry;
import io.datarouter.metric.links.MiscMetricsLinksRegistry;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.storage.metric.Gauges;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.listener.ComputedPropertiesAppListener;

public class DatarouterMetricsPlugin extends BaseWebPlugin{

	private final Class<? extends CountPublisher> countPublisher;
	private final Class<? extends GaugePublisher> gaugePublisher;
	private final Class<? extends MetricLinkBuilder> metricLinkBuilder;
	private final List<MetricName> metricNames;
	private final List<MetricDashboardDto> dashboards;
	private final List<MiscMetricLinksDto> miscMetricLinks;

	private DatarouterMetricsPlugin(
			DatarouterMetricsDaosModule daosModuleBuilder,
			Class<? extends CountPublisher> countPublisher,
			Class<? extends GaugePublisher> gaugePublisher,
			Class<? extends MetricLinkBuilder> metricLinkBuilder,
			boolean enableCountPublishing,
			boolean enableGaugePublishing,
			List<MetricName> metricNames,
			List<MetricDashboardDto> dashboards,
			List<MiscMetricLinksDto> miscMetricLinks){
		this.countPublisher = countPublisher;
		this.gaugePublisher = gaugePublisher;
		this.metricLinkBuilder = metricLinkBuilder;
		this.metricNames = metricNames;
		this.dashboards = dashboards;
		this.miscMetricLinks = miscMetricLinks;

		if(enableCountPublishing){
			addAppListenerOrdered(CountersAppListener.class, ComputedPropertiesAppListener.class);
			addPluginEntry(ConveyorConfigurationGroup.KEY, CountConveyorConfigurationGroup.class);
			addSettingRoot(DatarouterCountSettingRoot.class);
		}

		if(enableGaugePublishing){
			addPluginEntry(ConveyorConfigurationGroup.KEY, GaugeConveyorConfigurationGroup.class);
			addSettingRoot(DatarouterGaugeSettingRoot.class);
		}
		if(enableCountPublishing || enableGaugePublishing){
			addDynamicNavBarItem(MetricLinksNavBarItem.class);
		}

		addRouteSet(DatarouterMetricRouteSet.class);
		setDaosModule(daosModuleBuilder);
		addDatarouterGithubDocLink("datarouter-metric");
	}

	@Override
	public void configure(){
		bind(CountPublisher.class).to(countPublisher);
		bind(GaugePublisher.class).to(gaugePublisher);
		bindActual(Gauges.class, DatabeanGauges.class);
		bind(MetricNameRegistry.class).toInstance(new MetricNameRegistry(metricNames));
		bind(MetricDashboardRegistry.class).toInstance(new MetricDashboardRegistry(dashboards));
		bind(MiscMetricsLinksRegistry.class).toInstance(new MiscMetricsLinksRegistry(miscMetricLinks));
		bind(MetricLinkBuilder.class).to(metricLinkBuilder);
	}

	public static class DatarouterMetricsPluginBuilder{

		private final List<ClientId> metricQueueClientId;
		private final List<MetricName> metricNames;
		private final List<MetricDashboardDto> dashboards;
		private final List<MiscMetricLinksDto> miscMetricLinks;

		private Class<? extends CountPublisher> countPublisher = NoOpCountPublisher.class;
		private Class<? extends GaugePublisher> gaugePublisher = NoOpGaugePublisher.class;
		private Class<? extends MetricLinkBuilder> metricLinkBuilder = NoOpMetricLinkBuilder.class;

		private DatarouterMetricsDaosModule daosModule;

		public DatarouterMetricsPluginBuilder(
				List<ClientId> metricQueueClientId,
				Class<? extends CountPublisher> countPublisher,
				Class<? extends GaugePublisher> gaugePublisher){
			this.metricQueueClientId = metricQueueClientId;
			this.countPublisher = countPublisher;
			this.gaugePublisher = gaugePublisher;
			this.metricNames = new ArrayList<>();
			this.dashboards = new ArrayList<>();
			this.miscMetricLinks = new ArrayList<>();
		}

		public DatarouterMetricsPluginBuilder withDaosModule(DatarouterMetricsDaosModule daosModule){
			this.daosModule = daosModule;
			return this;
		}

		public DatarouterMetricsPluginBuilder withCountPublisher(Class<? extends CountPublisher> countPublisher){
			this.countPublisher = countPublisher;
			return this;
		}

		public DatarouterMetricsPluginBuilder withGaugePublisher(Class<? extends GaugePublisher> gaugePublisher){
			this.gaugePublisher = gaugePublisher;
			return this;
		}

		public DatarouterMetricsPluginBuilder addMetricName(MetricName name){
			this.metricNames.add(name);
			return this;
		}

		public DatarouterMetricsPluginBuilder addMetricNames(List<MetricName> names){
			this.metricNames.addAll(names);
			return this;
		}

		public DatarouterMetricsPluginBuilder addDashboard(MetricDashboardDto dashboard){
			this.dashboards.add(dashboard);
			return this;
		}

		public DatarouterMetricsPluginBuilder addDashboards(List<MetricDashboardDto> dashboards){
			this.dashboards.addAll(dashboards);
			return this;
		}

		public DatarouterMetricsPluginBuilder addMiscMetricLink(MiscMetricLinksDto miscMetricLink){
			this.miscMetricLinks.add(miscMetricLink);
			return this;
		}

		public DatarouterMetricsPluginBuilder addMiscMetricLinks(List<MiscMetricLinksDto> miscMetricLink){
			this.miscMetricLinks.addAll(miscMetricLink);
			return this;
		}

		public DatarouterMetricsPluginBuilder withMetricLinkBuilder(
				Class<? extends MetricLinkBuilder> metricLinkBuilder){
			this.metricLinkBuilder = metricLinkBuilder;
			return this;
		}

		public DatarouterMetricsPlugin build(){
			boolean enableCountPublishing = !countPublisher.isInstance(NoOpCountPublisher.class);
			boolean enableGaugePublishing = !gaugePublisher.isInstance(NoOpGaugePublisher.class);

			return new DatarouterMetricsPlugin(
					daosModule == null
							? new DatarouterMetricsDaosModule(
									metricQueueClientId,
									enableCountPublishing,
									enableGaugePublishing)
							: daosModule,
					countPublisher,
					gaugePublisher,
					metricLinkBuilder,
					enableCountPublishing,
					enableGaugePublishing,
					metricNames,
					dashboards,
					miscMetricLinks);
		}

	}

	public static class DatarouterMetricsDaosModule extends DaosModuleBuilder{

		private final List<ClientId> metricBlobQueueClientId;

		private final boolean enableCountPublishing;
		private final boolean enableGaugePublishing;

		public DatarouterMetricsDaosModule(
				List<ClientId> metricBlobQueueClientId,
				boolean enableCountPublishing,
				boolean enableGaugePublishing){
			this.metricBlobQueueClientId = metricBlobQueueClientId;
			this.enableCountPublishing = enableCountPublishing;
			this.enableGaugePublishing = enableGaugePublishing;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			List<Class<? extends Dao>> daos = new ArrayList<>();
			if(enableCountPublishing){
				daos.add(CountQueueDao.class);
			}
			if(enableGaugePublishing){
				daos.add(GaugeQueueDao.class);
			}
			return daos;
		}

		@Override
		public void configure(){
			if(enableCountPublishing){
				bind(DatarouterCountQueueDaoParams.class).toInstance(new DatarouterCountQueueDaoParams(
						metricBlobQueueClientId));
			}
			if(enableGaugePublishing){
				bind(GaugeQueueDaoParams.class).toInstance(new GaugeQueueDaoParams(
						metricBlobQueueClientId));
			}
		}

	}

}
