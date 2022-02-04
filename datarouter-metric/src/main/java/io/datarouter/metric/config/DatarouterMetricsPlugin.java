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

import io.datarouter.instrumentation.count.CountPublisher;
import io.datarouter.instrumentation.count.CountPublisher.NoOpCountPublisher;
import io.datarouter.instrumentation.gauge.GaugePublisher;
import io.datarouter.instrumentation.gauge.GaugePublisher.NoOpGaugePublisher;
import io.datarouter.instrumentation.metric.MetricLinkBuilder;
import io.datarouter.instrumentation.metric.MetricLinkBuilder.NoOpMetricLinkBuilder;
import io.datarouter.metric.counter.CountBlobDirectorySupplier;
import io.datarouter.metric.counter.CountBlobDirectorySupplier.NoOpCountBlobDirectorySupplier;
import io.datarouter.metric.counter.CountBlobPublishingSettings;
import io.datarouter.metric.counter.CountBlobPublishingSettings.NoOpCountBlobPublishingSettings;
import io.datarouter.metric.counter.CountersAppListener;
import io.datarouter.metric.counter.DatarouterCountBlobQueueDao;
import io.datarouter.metric.counter.DatarouterCountBlobQueueDao.DatarouterCountBlobQueueDaoParams;
import io.datarouter.metric.counter.DatarouterCountPublisherDao;
import io.datarouter.metric.counter.DatarouterCountPublisherDao.DatarouterCountPublisherDaoParams;
import io.datarouter.metric.counter.conveyor.CountConveyors;
import io.datarouter.metric.dto.MetricDashboardDto;
import io.datarouter.metric.dto.MetricName;
import io.datarouter.metric.dto.MiscMetricLinksDto;
import io.datarouter.metric.links.MetricDashboardRegistry;
import io.datarouter.metric.links.MetricNameRegistry;
import io.datarouter.metric.links.MiscMetricsLinksRegistry;
import io.datarouter.metric.metric.DatabeanGauges;
import io.datarouter.metric.metric.DatarouterGaugePublisherDao;
import io.datarouter.metric.metric.DatarouterGaugePublisherDao.DatarouterGaugePublisherDaoParams;
import io.datarouter.metric.metric.conveyor.GaugeConveyors;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.storage.metric.Gauges;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.listener.ComputedPropertiesAppListener;

public class DatarouterMetricsPlugin extends BaseWebPlugin{

	private final Class<? extends CountPublisher> countPublisher;
	private final Class<? extends CountBlobDirectorySupplier> countBlobDirectorySupplier;
	private final Class<? extends CountBlobPublishingSettings> countBlobPublishingSettings;
	private final Class<? extends GaugePublisher> gaugePublisher;
	private final Class<? extends MetricLinkBuilder> metricLinkBuilder;
	private final List<MetricName> metricNames;
	private final List<MetricDashboardDto> dashboards;
	private final List<MiscMetricLinksDto> miscMetricLinks;

	private DatarouterMetricsPlugin(
			DatarouterMetricsDaosModule daosModuleBuilder,
			Class<? extends CountPublisher> countPublisher,
			Class<? extends CountBlobDirectorySupplier> countBlobDirectorySupplier,
			Class<? extends CountBlobPublishingSettings> countBlobPublishingSettings,
			Class<? extends GaugePublisher> gaugePublisher,
			Class<? extends MetricLinkBuilder> metricLinkBuilder,
			boolean enableCountPublishing,
			boolean enableGaugePublishing,
			List<MetricName> metricNames,
			List<MetricDashboardDto> dashboards,
			List<MiscMetricLinksDto> miscMetricLinks){
		this.countPublisher = countPublisher;
		this.countBlobDirectorySupplier = countBlobDirectorySupplier;
		this.countBlobPublishingSettings = countBlobPublishingSettings;
		this.gaugePublisher = gaugePublisher;
		this.metricLinkBuilder = metricLinkBuilder;
		this.metricNames = metricNames;
		this.dashboards = dashboards;
		this.miscMetricLinks = miscMetricLinks;

		if(enableCountPublishing){
			addAppListener(CountConveyors.class);
			addAppListenerOrdered(CountersAppListener.class, ComputedPropertiesAppListener.class);
			addSettingRoot(DatarouterCountSettingRoot.class);
		}

		if(enableGaugePublishing){
			addAppListener(GaugeConveyors.class);
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
		bind(CountBlobDirectorySupplier.class).to(countBlobDirectorySupplier);
		bind(CountBlobPublishingSettings.class).to(countBlobPublishingSettings);
		bind(GaugePublisher.class).to(gaugePublisher);
		bindActual(Gauges.class, DatabeanGauges.class);
		bind(MetricNameRegistry.class).toInstance(new MetricNameRegistry(metricNames));
		bind(MetricDashboardRegistry.class).toInstance(new MetricDashboardRegistry(dashboards));
		bind(MiscMetricsLinksRegistry.class).toInstance(new MiscMetricsLinksRegistry(miscMetricLinks));
		bind(MetricLinkBuilder.class).to(metricLinkBuilder);
	}

	public static class DatarouterMetricsPluginBuilder{

		private final List<ClientId> defaultQueueClientId;
		private final List<ClientId> countBlobQueueClientId;
		private final List<MetricName> metricNames;
		private final List<MetricDashboardDto> dashboards;
		private final List<MiscMetricLinksDto> miscMetricLinks;

		private Class<? extends CountPublisher> countPublisher = NoOpCountPublisher.class;
		private Class<? extends CountBlobDirectorySupplier> countBlobDirectorySupplier =
				NoOpCountBlobDirectorySupplier.class;
		private Class<? extends CountBlobPublishingSettings> countBlobPublishingSettings =
				NoOpCountBlobPublishingSettings.class;
		private Class<? extends GaugePublisher> gaugePublisher = NoOpGaugePublisher.class;
		private Class<? extends MetricLinkBuilder> metricLinkBuilder = NoOpMetricLinkBuilder.class;

		private DatarouterMetricsDaosModule daosModule;

		public DatarouterMetricsPluginBuilder(
				List<ClientId> defaultQueueClientId,
				List<ClientId> countBlobQueueClientId,
				Class<? extends CountPublisher> countPublisher,
				Class<? extends CountBlobDirectorySupplier> countBlobDirectorySupplier,
				Class<? extends CountBlobPublishingSettings> countBlobPublishingSettings,
				Class<? extends GaugePublisher> gaugePublisher){
			this.defaultQueueClientId = defaultQueueClientId;
			this.countBlobQueueClientId = countBlobQueueClientId;
			this.countPublisher = countPublisher;
			this.countBlobDirectorySupplier = countBlobDirectorySupplier;
			this.countBlobPublishingSettings = countBlobPublishingSettings;
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

		public DatarouterMetricsPluginBuilder withCountBlobDirectorySupplier(
				Class<? extends CountBlobDirectorySupplier> countBlobDirectorySupplier){
			this.countBlobDirectorySupplier = countBlobDirectorySupplier;
			return this;
		}

		public DatarouterMetricsPluginBuilder withCountBlobPublishingSettings(
				Class<? extends CountBlobPublishingSettings> countBlobPublishingSettings){
			this.countBlobPublishingSettings = countBlobPublishingSettings;
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
									defaultQueueClientId,
									defaultQueueClientId,
									countBlobQueueClientId,
									enableCountPublishing,
									enableGaugePublishing)
							: daosModule,
					countPublisher,
					countBlobDirectorySupplier,
					countBlobPublishingSettings,
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

		private final List<ClientId> datarouterCountPublisherClientId;
		private final List<ClientId> datarouterGaugePublisherClientId;
		private final List<ClientId> datarouterCountBlobClientId;

		private final boolean enableCountPublishing;
		private final boolean enableGaugePublishing;

		public DatarouterMetricsDaosModule(
				List<ClientId> datarouterCountPublisherClientId,
				List<ClientId> datarouterGaugePublisherClientId,
				List<ClientId> datarouterCountBlobClientId,
				boolean enableCountPublishing,
				boolean enableGaugePublishing){
			this.datarouterCountPublisherClientId = datarouterCountPublisherClientId;
			this.datarouterGaugePublisherClientId = datarouterGaugePublisherClientId;
			this.datarouterCountBlobClientId = datarouterCountBlobClientId;
			this.enableCountPublishing = enableCountPublishing;
			this.enableGaugePublishing = enableGaugePublishing;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			List<Class<? extends Dao>> daos = new ArrayList<>();
			if(enableCountPublishing){
				daos.add(DatarouterCountPublisherDao.class);
			}
			daos.add(DatarouterCountBlobQueueDao.class);
			if(enableGaugePublishing){
				daos.add(DatarouterGaugePublisherDao.class);
			}
			return daos;
		}

		@Override
		public void configure(){
			if(enableCountPublishing){
				bind(DatarouterCountPublisherDaoParams.class).toInstance(new DatarouterCountPublisherDaoParams(
						datarouterCountPublisherClientId));
			}
			bind(DatarouterCountBlobQueueDaoParams.class).toInstance(new DatarouterCountBlobQueueDaoParams(
					datarouterCountBlobClientId));
			if(enableGaugePublishing){
				bind(DatarouterGaugePublisherDaoParams.class).toInstance(new DatarouterGaugePublisherDaoParams(
						datarouterGaugePublisherClientId));
			}
		}

	}

}
