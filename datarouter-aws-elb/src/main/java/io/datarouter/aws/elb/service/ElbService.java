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
package io.datarouter.aws.elb.service;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.elasticloadbalancingv2.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancingv2.AmazonElasticLoadBalancingClientBuilder;
import com.amazonaws.services.elasticloadbalancingv2.model.Action;
import com.amazonaws.services.elasticloadbalancingv2.model.DescribeListenersRequest;
import com.amazonaws.services.elasticloadbalancingv2.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancingv2.model.DescribeTargetHealthRequest;
import com.amazonaws.services.elasticloadbalancingv2.model.Listener;
import com.amazonaws.services.elasticloadbalancingv2.model.LoadBalancer;
import com.amazonaws.services.elasticloadbalancingv2.model.TargetDescription;
import com.amazonaws.services.elasticloadbalancingv2.model.TargetHealthDescription;

import io.datarouter.aws.elb.config.DatarouterAwsElbMonitoringSettings;
import io.datarouter.util.number.RandomTool;
import io.datarouter.util.retry.RetryableTool;
import io.datarouter.util.singletonsupplier.SingletonSupplier;

@Singleton
public class ElbService{

	private static final int NUM_ATTEMPTS = 3;

	private final Supplier<AmazonElasticLoadBalancing> amazonElasticLoadBalancing = SingletonSupplier.of(
			this::getAmazonElbClient);

	@Inject
	private DatarouterAwsElbMonitoringSettings settings;

	public List<LoadBalancer> getLoadBalancers(){
		var request = new DescribeLoadBalancersRequest();
		int randomSleepMs = RandomTool.getRandomIntBetweenTwoNumbers(0, 3_000);
		return RetryableTool.tryNTimesWithBackoffUnchecked(
				() -> amazonElasticLoadBalancing.get().describeLoadBalancers(request).getLoadBalancers(),
				NUM_ATTEMPTS,
				randomSleepMs,
				true);
	}

	public List<String> getTargetGroupsArn(String loadBalancerArn){
		var request = new DescribeListenersRequest().withLoadBalancerArn(loadBalancerArn);
		int randomSleepMs = RandomTool.getRandomIntBetweenTwoNumbers(0, 3_000);
		return RetryableTool.tryNTimesWithBackoffUnchecked(
				() -> amazonElasticLoadBalancing.get().describeListeners(request).getListeners().stream()
						.map(Listener::getDefaultActions)
						.flatMap(List::stream)
						.map(Action::getTargetGroupArn)
						.filter(Objects::nonNull) // remove action that have no tg (like redirect)
						.distinct()
						.collect(Collectors.toList()),
				NUM_ATTEMPTS,
				randomSleepMs,
				true);
	}

	public List<String> getTargetEc2InstancesId(String targetGroupArn){
		var request = new DescribeTargetHealthRequest().withTargetGroupArn(targetGroupArn);
		int randomSleepMs = RandomTool.getRandomIntBetweenTwoNumbers(0, 3_000);
		return RetryableTool.tryNTimesWithBackoffUnchecked(
				() -> amazonElasticLoadBalancing.get().describeTargetHealth(request).getTargetHealthDescriptions()
						.stream()
						.map(TargetHealthDescription::getTarget)
						.map(TargetDescription::getId)
						.collect(Collectors.toList()),
				NUM_ATTEMPTS,
				randomSleepMs,
				true);
	}

	private AmazonElasticLoadBalancing getAmazonElbClient(){
		AWSCredentials awsCredentials = new BasicAWSCredentials(settings.accessKey.get(), settings.secretKey.get());
		return AmazonElasticLoadBalancingClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
				.withRegion(settings.region.get())
				.build();
	}

}
