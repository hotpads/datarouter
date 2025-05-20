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
import io.datarouter.instrumentation.metric.MetricLinkBuilder;
import io.datarouter.instrumentation.metric.MetricLinkBuilder.NoOpMetricLinkBuilder;
import io.datarouter.metric.dashboard.MetricDashboardDto;
import io.datarouter.metric.dashboard.MetricDashboardRegistry;
import io.datarouter.metric.dashboard.MetricName;
import io.datarouter.metric.dashboard.MetricNameRegistry;
import io.datarouter.metric.dashboard.MiscMetricLinksDto;
import io.datarouter.metric.dashboard.MiscMetricsLinksRegistry;
import io.datarouter.metric.publisher.MetricAnnotationNonProdQueueDao;
import io.datarouter.metric.publisher.MetricAnnotationNonProdQueueDao.MetricAnnotationNonProdQueueDaoParams;
import io.datarouter.metric.publisher.MetricAnnotationPublisher;
import io.datarouter.metric.publisher.MetricAnnotationPublisher.NoOpMetricAnnotationPublisher;
import io.datarouter.metric.publisher.MetricAnnotationQueueDao;
import io.datarouter.metric.publisher.MetricAnnotationQueueDao.MetricAnnotationQueueDaoParams;
import io.datarouter.metric.publisher.MetricNonProdQueueDao;
import io.datarouter.metric.publisher.MetricNonProdQueueDao.MetricNonProdQueueDaoParams;
import io.datarouter.metric.publisher.MetricPublisher;
import io.datarouter.metric.publisher.MetricPublisher.NoOpMetricPublisher;
import io.datarouter.metric.publisher.MetricQueueDao;
import io.datarouter.metric.publisher.MetricQueueDao.MetricQueueDaoParams;
import io.datarouter.metric.service.UnexpectedHandlerUsageDailyDigest;
import io.datarouter.metric.template.MetricTemplateAppListener;
import io.datarouter.metric.template.MetricTemplateConveyorConfigurationGroup;
import io.datarouter.metric.template.MetricTemplatePublisher;
import io.datarouter.metric.template.MetricTemplatePublisher.NoOpMetricTemplatePublisher;
import io.datarouter.metric.template.MetricTemplateQueueDao;
import io.datarouter.metric.template.MetricTemplateQueueDao.MetricTemplateQueueDaoParams;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.handlerusage.HandlerUsageBuilder;
import io.datarouter.web.handlerusage.HandlerUsageBuilder.NoOpHandlerUsageBuilder;
import io.datarouter.web.indexusage.IndexUsageBuilder;
import io.datarouter.web.indexusage.IndexUsageBuilder.NoOpIndexUsageBuilder;
import io.datarouter.web.listener.ComputedPropertiesAppListener;

public class DatarouterMetricsPlugin extends BaseWebPlugin{

	private final Class<? extends MetricPublisher> metricPublisher;
	private final Class<? extends MetricAnnotationPublisher> metricAnnotationPublisher;
	private final Class<? extends MetricTemplatePublisher> metricTemplatePublisher;
	private final Class<? extends MetricLinkBuilder> metricLinkBuilder;
	private final Class<? extends HandlerUsageBuilder> endpointUsageBuilder;
	private final Class<? extends IndexUsageBuilder> indexUsageBuilder;
	private final List<MetricName> metricNames;
	private final List<MetricDashboardDto> dashboards;
	private final List<MiscMetricLinksDto> miscMetricLinks;

	private DatarouterMetricsPlugin(
			DatarouterMetricsDaosModule daosModuleBuilder,
			Class<? extends MetricPublisher> metricPublisher,
			Class<? extends MetricAnnotationPublisher> metricAnnotationPublisher,
			Class<? extends MetricTemplatePublisher> metricTemplatePublisher,
			Class<? extends MetricLinkBuilder> metricLinkBuilder,
			Class<? extends HandlerUsageBuilder> endpointUsageBuilder,
			Class<? extends IndexUsageBuilder> indexUsageBuilder,
			boolean enableMetricPublishing,
			boolean enableMetricTemplatePublishing,
			List<MetricName> metricNames,
			List<MetricDashboardDto> dashboards,
			List<MiscMetricLinksDto> miscMetricLinks){
		this.metricPublisher = metricPublisher;
		this.metricAnnotationPublisher = metricAnnotationPublisher;
		this.metricTemplatePublisher = metricTemplatePublisher;
		this.metricLinkBuilder = metricLinkBuilder;
		this.endpointUsageBuilder = endpointUsageBuilder;
		this.indexUsageBuilder = indexUsageBuilder;
		this.metricNames = metricNames;
		this.dashboards = dashboards;
		this.miscMetricLinks = miscMetricLinks;

		if(enableMetricPublishing){
			addAppListenerOrdered(DatarouterMetricAppListener.class, ComputedPropertiesAppListener.class);
			addPluginEntry(ConveyorConfigurationGroup.KEY, DatarouterMetricConveyorConfigurationGroup.class);
			addSettingRoot(DatarouterMetricSettingRoot.class);
			addDynamicNavBarItem(MetricLinksNavBarItem.class);
			addDynamicNavBarItem(HandlerUsageNavBarItem.class);
			addDynamicNavBarItem(IndexUsageNavBarItem.class);
		}
		if(enableMetricTemplatePublishing){
			addAppListener(MetricTemplateAppListener.class);
			addPluginEntry(ConveyorConfigurationGroup.KEY, MetricTemplateConveyorConfigurationGroup.class);
			addSettingRoot(DatarouterMetricTemplateSettingRoot.class);
		}

		addRouteSet(DatarouterMetricRouteSet.class);
		setDaosModule(daosModuleBuilder);
		addDatarouterGithubDocLink("datarouter-metric");
		addDailyDigest(UnexpectedHandlerUsageDailyDigest.class);
	}

	@Override
	public void configure(){
		bind(MetricPublisher.class).to(metricPublisher);
		bind(MetricAnnotationPublisher.class).to(metricAnnotationPublisher);
		bind(MetricTemplatePublisher.class).to(metricTemplatePublisher);
		bind(MetricNameRegistry.class).toInstance(new MetricNameRegistry(metricNames));
		bind(MetricDashboardRegistry.class).toInstance(new MetricDashboardRegistry(dashboards));
		bind(MiscMetricsLinksRegistry.class).toInstance(new MiscMetricsLinksRegistry(miscMetricLinks));
		bindActual(MetricLinkBuilder.class, metricLinkBuilder);
		bind(HandlerUsageBuilder.class).to(endpointUsageBuilder);
		bind(IndexUsageBuilder.class).to(indexUsageBuilder);
	}

	public static class DatarouterMetricsPluginBuilder{

		private final ClientId metricQueueClientId;
		private final ClientId metricNonProdQueueClientId;
		private final List<MetricName> metricNames;
		private final List<MetricDashboardDto> dashboards;
		private final List<MiscMetricLinksDto> miscMetricLinks;

		private Class<? extends MetricPublisher> metricPublisher = NoOpMetricPublisher.class;
		private Class<? extends MetricAnnotationPublisher> metricAnnotationPublisher =
				NoOpMetricAnnotationPublisher.class;
		private Class<? extends MetricTemplatePublisher> metricTemplatePublisher = NoOpMetricTemplatePublisher.class;
		private Class<? extends MetricLinkBuilder> metricLinkBuilder = NoOpMetricLinkBuilder.class;
		private Class<? extends HandlerUsageBuilder> handlerUsageBuilder = NoOpHandlerUsageBuilder.class;
		private Class<? extends IndexUsageBuilder> indexUsageBuilder = NoOpIndexUsageBuilder.class;

		private DatarouterMetricsDaosModule daosModule;

		public DatarouterMetricsPluginBuilder(
				ClientId metricQueueClientId,
				ClientId metricNonProdQueueClientId,
				//TODO should these be set via the builder methods below?
				Class<? extends MetricPublisher> metricPublisher,
				Class<? extends MetricAnnotationPublisher> metricAnnotationPublisher,
				Class<? extends MetricTemplatePublisher> metricTemplatePublisher){
			this.metricQueueClientId = metricQueueClientId;
			this.metricNonProdQueueClientId = metricNonProdQueueClientId;
			this.metricPublisher = metricPublisher;
			this.metricAnnotationPublisher = metricAnnotationPublisher;
			this.metricTemplatePublisher = metricTemplatePublisher;
			this.metricNames = new ArrayList<>();
			this.dashboards = new ArrayList<>();
			this.miscMetricLinks = new ArrayList<>();
		}

		public DatarouterMetricsPluginBuilder withDaosModule(DatarouterMetricsDaosModule daosModule){
			this.daosModule = daosModule;
			return this;
		}

		public DatarouterMetricsPluginBuilder withMetricPublisher(Class<? extends MetricPublisher> metricPublisher){
			this.metricPublisher = metricPublisher;
			return this;
		}

		public DatarouterMetricsPluginBuilder withMetricTemplatePublisher(
				Class<? extends MetricTemplatePublisher> metricTemplatePublisher){
			this.metricTemplatePublisher = metricTemplatePublisher;
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

		public DatarouterMetricsPluginBuilder withHandlerUsageBuilder(
				Class<? extends HandlerUsageBuilder> handlerUsageBuilder){
			this.handlerUsageBuilder = handlerUsageBuilder;
			return this;
		}

		public DatarouterMetricsPluginBuilder withIndexUsageBuilder(
				Class<? extends IndexUsageBuilder> indexUsageBuilder){
			this.indexUsageBuilder = indexUsageBuilder;
			return this;
		}

		public DatarouterMetricsPlugin build(){
			boolean enableMetricPublishing = !metricPublisher.isInstance(NoOpMetricPublisher.class);
			boolean enableMetricAnnotationPublishing =
					!metricAnnotationPublisher.isInstance(NoOpMetricAnnotationPublisher.class);
			boolean enableMetricTemplatePublishing = !metricTemplatePublisher
					.isInstance(NoOpMetricTemplatePublisher.class);

			return new DatarouterMetricsPlugin(
					daosModule == null
							? new DatarouterMetricsDaosModule(
									metricQueueClientId,
									metricNonProdQueueClientId,
									enableMetricPublishing,
									enableMetricAnnotationPublishing,
									enableMetricTemplatePublishing)
							: daosModule,
					metricPublisher,
					metricAnnotationPublisher,
					metricTemplatePublisher,
					metricLinkBuilder,
					handlerUsageBuilder,
					indexUsageBuilder,
					enableMetricPublishing,
					enableMetricTemplatePublishing,
					metricNames,
					dashboards,
					miscMetricLinks);
		}

	}

	public static class DatarouterMetricsDaosModule extends DaosModuleBuilder{

		private final ClientId metricBlobQueueClientId;
		private final ClientId metricNonProdBlobQueueClientId;

		private final boolean enableMetricPublishing;
		private final boolean enableMetricAnnotationPublishing;
		private final boolean enableMetricTemplatePublishing;

		public DatarouterMetricsDaosModule(
				ClientId metricBlobQueueClientId,
				ClientId metricNonProdBlobQueueClientId,
				boolean enableMetricPublishing,
				boolean enableMetricAnnotationPublishing,
				boolean enableMetricTemplatePublishing){
			this.metricBlobQueueClientId = metricBlobQueueClientId;
			this.metricNonProdBlobQueueClientId = metricNonProdBlobQueueClientId;
			this.enableMetricPublishing = enableMetricPublishing;
			this.enableMetricAnnotationPublishing = enableMetricAnnotationPublishing;
			this.enableMetricTemplatePublishing = enableMetricTemplatePublishing;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			List<Class<? extends Dao>> daos = new ArrayList<>();
			if(enableMetricPublishing){
				daos.add(MetricQueueDao.class);
				daos.add(MetricNonProdQueueDao.class);
			}
			if(enableMetricAnnotationPublishing){
				daos.add(MetricAnnotationQueueDao.class);
				daos.add(MetricAnnotationNonProdQueueDao.class);
			}
			if(enableMetricTemplatePublishing){
				daos.add(MetricTemplateQueueDao.class);
			}
			return daos;
		}

		@Override
		public void configure(){
			if(enableMetricPublishing){
				bind(MetricQueueDaoParams.class)
						.toInstance(new MetricQueueDaoParams(metricBlobQueueClientId));
				bind(MetricNonProdQueueDaoParams.class)
						.toInstance(new MetricNonProdQueueDaoParams(metricNonProdBlobQueueClientId));
			}
			if(enableMetricAnnotationPublishing){
				bind(MetricAnnotationQueueDaoParams.class)
						.toInstance(new MetricAnnotationQueueDaoParams(metricBlobQueueClientId));
				bind(MetricAnnotationNonProdQueueDaoParams.class)
						.toInstance(new MetricAnnotationNonProdQueueDaoParams(metricNonProdBlobQueueClientId));
			}
			if(enableMetricTemplatePublishing){
				bind(MetricTemplateQueueDaoParams.class)
						.toInstance(new MetricTemplateQueueDaoParams(metricBlobQueueClientId));
			}
		}

	}

}
