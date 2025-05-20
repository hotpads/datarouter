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
package io.datarouter.aws.sqs;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.aws.sqs.config.DatarouterSqsSettingsRoot;
import io.datarouter.instrumentation.metric.Metrics;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.BaseClientManager;
import io.datarouter.storage.client.ClientId;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataRequest.Builder;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataResponse;
import software.amazon.awssdk.services.cloudwatch.model.Metric;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataQuery;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.cloudwatch.model.MetricStat;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.SetQueueAttributesRequest;

@Singleton
public class SqsClientManager extends BaseClientManager{
	private static final Logger logger = LoggerFactory.getLogger(SqsClientManager.class);

	@Inject
	private AmazonSqsHolder amazonSqsHolder;
	@Inject
	private DatarouterSqsSettingsRoot datarouterSqsSettingsRoot;

	@Override
	public void shutdown(ClientId clientId){
		amazonSqsHolder.get(clientId).close();
	}

	@Override
	protected void safeInitClient(ClientId clientId){
		amazonSqsHolder.registerClient(clientId);
	}

	public SqsClient getAmazonSqs(ClientId clientId){
		initClient(clientId);
		return amazonSqsHolder.get(clientId);
	}

	public String getQueueAttribute(ClientId clientId, String queueUrl, QueueAttributeName attributeName){
		return getQueueAttributes(clientId, queueUrl, List.of(attributeName)).get(attributeName);
	}

	public Map<QueueAttributeName,String> getAllQueueAttributes(ClientId clientId, String sqsQueueUrl){
		return getQueueAttributes(clientId, sqsQueueUrl, List.of(QueueAttributeName.ALL));
	}

	private MetricDataQuery createMetricDataQuery(String queueName){
		Metric metric = Metric.builder()
				.metricName("ApproximateAgeOfOldestMessage")
				.dimensions(Dimension.builder()
						.name("QueueName")
						.value(queueName)
						.build())
				.namespace("AWS/SQS")
				.build();
		MetricStat metricStat = MetricStat.builder()
				.stat("Average")
				.period(60)
				.metric(metric)
				.build();
		return MetricDataQuery.builder()
				.id(queueName.replace("-", "").replace(":", ""))
				.metricStat(metricStat)
				.build();
	}

	public Map<String,Long> getApproximateAgeOfOldestUnackedMessageSecondsGroup(ClientId clientId,
			List<String> queueNames){
		count("queue", queueNames.size());
		Map<String,Long> queueNamesAndApproximateAgeOfOldestMessage = new HashMap<>();
		CloudWatchClient cloudWatch = amazonSqsHolder.getCloudWatch(clientId);
		if(cloudWatch == null){
			logger.error("CloudwatchClient is null for clientid={}", clientId);
			return Map.of();
		}
		List<MetricDataQuery> metricDataQueries = Scanner.of(queueNames)
				.map(this::createMetricDataQuery)
				.list();
		String nextToken = null;
		// end is exclusive
		Instant endTime = Instant.now().plus(1, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MINUTES);
		// 3 data point max (sometime only one is there)
		Instant startTime = endTime.minus(datarouterSqsSettingsRoot.cloudWatchMinuteBack.get(), ChronoUnit.MINUTES);
		int requestCount = 0;
		int resultCount = 0;
		var datapointCount = new LongAdder();
		try{
			do{
				Builder requestBuilder = GetMetricDataRequest.builder()
						.startTime(startTime)
						.endTime(endTime)
						.metricDataQueries(metricDataQueries);
				if(nextToken != null){
					requestBuilder.nextToken(nextToken);
				}
				GetMetricDataResponse response = cloudWatch.getMetricData(requestBuilder.build());
				nextToken = response.nextToken();
				requestCount++;
				count("request", 1);
				List<MetricDataResult> result = response.metricDataResults();
				resultCount += result.size();
				count("result", result.size());
				result.forEach(res -> {
					logger.debug("{}", res);
					datapointCount.add(res.values().size());
					count("datapoint", res.values().size());
					String queueName = res.label();
					if(res.values().isEmpty()){
						logger.warn("no data for {}", queueName);
					}else{
						count("queueWithData", 1);
						// most recent value is first
						queueNamesAndApproximateAgeOfOldestMessage.put(queueName, res.values().getFirst().longValue());
					}
				});
			}while(nextToken != null);
		}catch(RuntimeException e){
			logger.error("Failed to obtain metrics from cloudwatch", e);
			return Map.of();
		}
		logger.warn("start={} end={} queues={} requests={} result={} data={}", startTime, endTime, queueNames.size(),
				requestCount, resultCount, datapointCount.sum());
		logger.debug("result={}", queueNamesAndApproximateAgeOfOldestMessage);
		return queueNamesAndApproximateAgeOfOldestMessage;
	}

	private void count(String string, long delta){
		Metrics.count("GetMetricData ApproximateAgeOfOldestMessage " + string, delta);
	}

	public Map<QueueAttributeName,String> getQueueAttributes(
			ClientId clientId,
			String queueUrl,
			List<QueueAttributeName> attributes){
		var request = GetQueueAttributesRequest.builder()
				.attributeNames(attributes)
				.queueUrl(queueUrl)
				.build();
		return getAmazonSqs(clientId).getQueueAttributes(request).attributes();
	}

	public void updateAttr(ClientId clientId, String queueUrl, QueueAttributeName key, Object value){
		Map<QueueAttributeName,String> attributes = Map.of(key, String.valueOf(value));
		var request = SetQueueAttributesRequest.builder()
						.attributes(attributes)
						.queueUrl(queueUrl)
						.build();
		getAmazonSqs(clientId).setQueueAttributes(request);
	}

}
