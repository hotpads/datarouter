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
package io.datarouter.web.plugins.opencencus.metrics;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.gauge.Gauges;
import io.datarouter.plugin.PluginInjector;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.listener.DatarouterAppListener;
import io.opencensus.common.Duration;
import io.opencensus.exporter.metrics.util.IntervalMetricReader;
import io.opencensus.exporter.metrics.util.MetricExporter;
import io.opencensus.exporter.metrics.util.MetricReader;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.Metrics;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.metrics.export.Point;
import io.opencensus.metrics.export.TimeSeries;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class OpencencusMetricsAppListener implements DatarouterAppListener{
	private static final Logger logger = LoggerFactory.getLogger(OpencencusMetricsAppListener.class);

	@Inject
	private PluginInjector pluginInjector;
	@Inject
	private DifferencingCounterService differencingCounterService;

	private IntervalMetricReader metricReader;

	@Override
	public void onStartUp(){
		MetricReader reader = MetricReader.create(MetricReader.Options.builder()
				.setMetricProducerManager(Metrics.getExportComponent().getMetricProducerManager())
				.setSpanName("datarouter metrics")
				.build());
		var exporter = new DatarouterMetricExporter();

		IntervalMetricReader.Options options = IntervalMetricReader.Options.builder()
				.setExportInterval(Duration.create(5, 0))
				.build();
		metricReader = IntervalMetricReader.create(exporter, reader, options);
	}

	@Override
	public void onShutDown(){
		if(metricReader != null){
			metricReader.stop();
		}
	}

	private class DatarouterMetricExporter extends MetricExporter{

		@Override
		public void export(Collection<Metric> metrics){
			List<OpencencusMetricsMapper> mappers = pluginInjector.getInstances(OpencencusMetricsMapper.KEY);
			logger.info("got metricCount={} mapperCount={}",
					metrics.size(),
					mappers == null ? "null" : mappers.size());
			if(mappers == null){
				return;
			}
			for(Metric metric : metrics){
				String name = metric.getMetricDescriptor().getName();
				Type type = metric.getMetricDescriptor().getType();
				for(TimeSeries timeSeries : metric.getTimeSeriesList()){
					List<String> labels = Scanner.of(timeSeries.getLabelValues())
							.map(LabelValue::getValue)
							.list();
					List<Point> points = timeSeries.getPoints();
					if(points.size() < 1){
						logger.warn("no points metric={}", metric);
						break;
					}
					if(points.size() > 1){
						logger.warn("too many points metric={}", metric);
					}
					Long value = points.get(0).getValue().match(
							doubleValue -> null,
							longValue -> longValue,
							distributionValue -> null,
							summaryValue -> null,
							ocValue -> null);
					var metricDto = new OpencencusMetricsDto(name, labels);
					sendMetricToMapper(metricDto, type, mappers, value);
				}
			}
		}

	}

	private void sendMetricToMapper(
			OpencencusMetricsDto metricDto,
			Type type,
			List<OpencencusMetricsMapper> mappers,
			Long value){
		for(OpencencusMetricsMapper metricsMapper : mappers){
			for(Function<OpencencusMetricsDto,String> function : metricsMapper.getMappers()){
				String datarouterMetricName = function.apply(metricDto);
				if(datarouterMetricName == null){
					continue;
				}
				switch(type){
				case GAUGE_INT64 -> Gauges.save(datarouterMetricName, value);
				case CUMULATIVE_INT64 -> differencingCounterService.add(datarouterMetricName, value);
				default -> throw new IllegalArgumentException("Unexpected value=" + type);
				}
				return;
			}
		}
	}

	public record OpencencusMetricsDto(
			String name,
			List<String> labels){
	}

}