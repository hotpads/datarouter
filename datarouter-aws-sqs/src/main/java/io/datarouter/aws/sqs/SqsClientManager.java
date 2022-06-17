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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.http.IdleConnectionReaper;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.QueueAttributeName;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.BaseClientManager;
import io.datarouter.storage.client.ClientId;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataResponse;
import software.amazon.awssdk.services.cloudwatch.model.Metric;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataQuery;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.cloudwatch.model.MetricStat;

@Singleton
public class SqsClientManager extends BaseClientManager{
	private static final Logger logger = LoggerFactory.getLogger(SqsClientManager.class);

	@Inject
	private AmazonSqsHolder amazonSqsHolder;

	@Override
	public void shutdown(ClientId clientId){
		amazonSqsHolder.get(clientId).shutdown();
		IdleConnectionReaper.shutdown();
	}

	@Override
	protected void safeInitClient(ClientId clientId){
		amazonSqsHolder.registerClient(clientId);
	}

	public AmazonSQS getAmazonSqs(ClientId clientId){
		initClient(clientId);
		return amazonSqsHolder.get(clientId);
	}

	public String getQueueAttribute(ClientId clientId, String queueUrl, QueueAttributeName attributeName){
		return getQueueAttributes(clientId, queueUrl, List.of(attributeName.name())).get(attributeName.name());
	}

	public Map<String,String> getAllQueueAttributes(ClientId clientId, String sqsQueueUrl){
		return getQueueAttributes(clientId, sqsQueueUrl, List.of(QueueAttributeName.All.name()));
	}

	private MetricDataQuery createMetricDataQuery(String queueName){
		Metric metric = Metric
				.builder()
				.metricName("ApproximateAgeOfOldestMessage")
				.dimensions(Dimension.builder()
						.name("QueueName")
						.value(queueName)
						.build())
				.namespace("AWS/SQS")
				.build();
		MetricStat metricStat = MetricStat
				.builder()
				.stat("Average")
				.period(60)
				.metric(metric)
				.build();
		MetricDataQuery metricDataQuery = MetricDataQuery
				.builder()
				.id(queueName.replace("-", ""))
				.label("ApproximateAgeOfOldestMessage")
				.returnData(true)
				.metricStat(metricStat)
				.build();
		return metricDataQuery;
	}

	public Map<String,Optional<Double>> getApproximateAgeOfOldestUnackedMessageSecondsGroup(ClientId clientId,
			List<String> queueNames){
		Map<String,Optional<Double>> queueNamesAndApproximateAgeOfOldestMessage = new HashMap<>();
		CloudWatchClient cloudWatch = amazonSqsHolder.getCloudWatch(clientId);
		if(cloudWatch == null){
			logger.error("CloudwatchClient is null for clientid={}", clientId);
			return Map.of();
		}
		List<MetricDataQuery> metricDataQueries = Scanner.of(queueNames)
				.map(this::createMetricDataQuery)
				.list();
		GetMetricDataRequest getMetricDataRequest;
		boolean done = false;
		String nextToken = null;
		List<MetricDataResult> result = new ArrayList<>();
		Instant startTime = Instant.now().minus(5, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MINUTES);
		Instant endTime = Instant.now().truncatedTo(ChronoUnit.MINUTES);
		try{
			while(!done){
				GetMetricDataResponse response;
				if(nextToken == null){
					getMetricDataRequest = GetMetricDataRequest.builder()
							.maxDatapoints(10)
							.startTime(startTime)
							.endTime(endTime)
							.metricDataQueries(metricDataQueries)
							.build();
					response = cloudWatch.getMetricData(getMetricDataRequest);
				}else{
					getMetricDataRequest = GetMetricDataRequest.builder()
							.maxDatapoints(10)
							.startTime(startTime)
							.endTime(endTime)
							.metricDataQueries(metricDataQueries)
							.nextToken(nextToken)
							.build();
					response = cloudWatch.getMetricData(getMetricDataRequest);
				}

				result = response.metricDataResults();
				result.forEach(res -> {
					if(!res.values().isEmpty()){
						queueNamesAndApproximateAgeOfOldestMessage.put(res.id(), Optional.of(res.values().get(0)));
					}
				});
				if(response.nextToken() == null){
					done = true;
				}else{
					nextToken = response.nextToken();
					logger.info("nextToken={}", nextToken);
				}
			}

		}catch(RuntimeException e){
			logger.error("Failed to obtain metrics from cloudwatch", e);
			return Map.of();
		}
		logger.info("result={}", result);
		return queueNamesAndApproximateAgeOfOldestMessage;
	}

	public Map<String,String> getQueueAttributes(ClientId clientId, String queueUrl, List<String> attributes){
		return getAmazonSqs(clientId).getQueueAttributes(queueUrl, attributes).getAttributes();
	}

	public void updateAttr(ClientId clientId, String queueUrl, QueueAttributeName key, Object value){
		Map<String,String> attributes = Map.of(key.name(), String.valueOf(value));
		getAmazonSqs(clientId).setQueueAttributes(queueUrl, attributes);
	}

}
