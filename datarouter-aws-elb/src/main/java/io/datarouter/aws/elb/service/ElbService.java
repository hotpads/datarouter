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
package io.datarouter.aws.elb.service;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import io.datarouter.aws.elb.config.DatarouterAwsElbMonitoringSettings;
import io.datarouter.util.number.RandomTool;
import io.datarouter.util.retry.RetryableTool;
import io.datarouter.util.singletonsupplier.SingletonSupplier;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Action;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeListenersRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeLoadBalancersRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeTargetHealthRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Listener;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetDescription;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetHealthDescription;

@Singleton
public class ElbService{

	private static final int NUM_ATTEMPTS = 3;

	private final Supplier<ElasticLoadBalancingV2Client> amazonElasticLoadBalancing = SingletonSupplier.of(
			this::getAmazonElbClient);

	@Inject
	private DatarouterAwsElbMonitoringSettings settings;

	public List<LoadBalancer> getLoadBalancers(){
		var request = DescribeLoadBalancersRequest.builder().build();
		int randomSleepMs = RandomTool.getRandomIntBetweenTwoNumbers(0, 3_000);
		return RetryableTool.tryNTimesWithBackoffAndRandomInitialDelayUnchecked(
				() -> amazonElasticLoadBalancing.get().describeLoadBalancers(request).loadBalancers(),
				NUM_ATTEMPTS,
				randomSleepMs,
				true);
	}

	public List<String> getTargetGroupsArn(String loadBalancerArn){
		var request = DescribeListenersRequest.builder().loadBalancerArn(loadBalancerArn).build();
		int randomSleepMs = RandomTool.getRandomIntBetweenTwoNumbers(0, 3_000);
		return RetryableTool.tryNTimesWithBackoffAndRandomInitialDelayUnchecked(
				() -> amazonElasticLoadBalancing.get().describeListeners(request).listeners().stream()
						.map(Listener::defaultActions)
						.flatMap(List::stream)
						.map(Action::targetGroupArn)
						.filter(Objects::nonNull) // remove action that have no tg (like redirect)
						.distinct()
						.toList(),
				NUM_ATTEMPTS,
				randomSleepMs,
				true);
	}

	public List<String> getTargetEc2InstancesId(String targetGroupArn){
		var request = DescribeTargetHealthRequest.builder().targetGroupArn(targetGroupArn).build();
		int randomSleepMs = RandomTool.getRandomIntBetweenTwoNumbers(0, 3_000);
		return RetryableTool.tryNTimesWithBackoffAndRandomInitialDelayUnchecked(
				() -> amazonElasticLoadBalancing.get().describeTargetHealth(request).targetHealthDescriptions()
						.stream()
						.map(TargetHealthDescription::target)
						.map(TargetDescription::id)
						.toList(),
				NUM_ATTEMPTS,
				randomSleepMs,
				true);
	}

	private ElasticLoadBalancingV2Client getAmazonElbClient(){
		AwsCredentials awsCredentials = AwsBasicCredentials.create(settings.accessKey.get(), settings.secretKey.get());
		return ElasticLoadBalancingV2Client.builder()
				.credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
				.region(Region.of(settings.region.get()))
				.build();
	}

}
