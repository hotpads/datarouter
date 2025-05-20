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
package io.datarouter.aws.rds.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.aws.rds.config.DatarouterAwsRdsConfigSettings;
import io.datarouter.aws.rds.config.DatarouterAwsRdsConfigSettings.RdsCredentialsDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.number.RandomTool;
import io.datarouter.util.retry.RetryableTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.AddTagsToResourceRequest;
import software.amazon.awssdk.services.rds.model.CreateDbInstanceRequest;
import software.amazon.awssdk.services.rds.model.DBCluster;
import software.amazon.awssdk.services.rds.model.DBClusterMember;
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.rds.model.DeleteDbInstanceRequest;
import software.amazon.awssdk.services.rds.model.DescribeBlueGreenDeploymentsRequest;
import software.amazon.awssdk.services.rds.model.DescribeBlueGreenDeploymentsResponse;
import software.amazon.awssdk.services.rds.model.DescribeDbClustersRequest;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesRequest;
import software.amazon.awssdk.services.rds.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.rds.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.rds.model.Tag;

@Singleton
public class RdsService{
	private static final Logger logger = LoggerFactory.getLogger(RdsService.class);

	private static final int NUM_ATTEMPTS = 5;
	private static final String AVAILABLE_STATUS = "available";

	@Inject
	private DatarouterAwsRdsConfigSettings rdsSettings;

	public List<String> getReaderInstanceIds(String clusterName, String region){
		return getCluster(clusterName, region).dbClusterMembers().stream()
				.filter(m -> !m.isClusterWriter())
				.map(DBClusterMember::dbInstanceIdentifier)
				.toList();
	}

	public boolean isReaderInstance(String instanceName, String clusterName, String region){
		return Scanner.of(getCluster(clusterName, region).dbClusterMembers())
				.include(clusterMember -> !clusterMember.isClusterWriter())
				.map(DBClusterMember::dbInstanceIdentifier)
				.anyMatch(instanceId -> instanceId.equals(instanceName));
	}

	public DBInstance getInstance(String instanceName, String region){
		return getInstance(instanceName, region, NUM_ATTEMPTS);
	}

	public DBInstance getInstance(String instanceName, String region, int numRetryAttempts){
		var request = DescribeDbInstancesRequest.builder().dbInstanceIdentifier(instanceName).build();
		int randomSleepMs = RandomTool.getRandomIntBetweenTwoNumbers(0, 3_000);
		return RetryableTool.tryNTimesWithBackoffAndRandomInitialDelayUnchecked(
				() -> getAmazonRdsReadOnlyClient(region).describeDBInstances(request).dbInstances().getFirst(),
				numRetryAttempts,
				randomSleepMs,
				true);
	}

	public DBInstance getWriterInstance(String clusterName, String region){
		Optional<String> writerInstance = Scanner.of(getCluster(clusterName, region).dbClusterMembers())
				.include(DBClusterMember::isClusterWriter)
				.map(DBClusterMember::dbInstanceIdentifier)
				.findFirst();
		return getInstance(writerInstance.get(), region);
	}

	public void createOtherInstance(String clusterName, String region){
		String otherInstanceName = clusterName + rdsSettings.dbOtherInstanceSuffix.get();
		logger.warn("Request to create other instance={} for cluster={}", otherInstanceName, clusterName);
		if(!getReaderInstanceIds(clusterName, region).contains(otherInstanceName)){
			String availabilityZone = getWriterInstance(clusterName, region).availabilityZone();
			createDbInstance(otherInstanceName, clusterName, region, availabilityZone);
		}
	}

	public void deleteOtherInstance(String instanceName, String region){
		var describeRequest = DescribeDbInstancesRequest.builder()
				.dbInstanceIdentifier(instanceName)
				.build();
		String instanceStatus = getAmazonRdsCreateOtherClient(region)
				.describeDBInstances(describeRequest)
				.dbInstances()
				.getFirst()
				.dbInstanceStatus();
		if(instanceStatus.equals(AVAILABLE_STATUS) && instanceName.endsWith(
				rdsSettings.dbOtherInstanceSuffix.get())){
			var request = DeleteDbInstanceRequest.builder()
					.dbInstanceIdentifier(instanceName)
					.build();
			getAmazonRdsCreateOtherClient(region).deleteDBInstance(request);
		}
	}

	public void createDbInstance(String instanceName, String clusterName, String region, String availabilityZone){
		String parameterGroup = rdsSettings.getParameterGroup(region);
		var request = CreateDbInstanceRequest.builder()
				.dbInstanceIdentifier(instanceName)
				.dbInstanceClass(rdsSettings.dbOtherInstanceClass.get())
				.engine(rdsSettings.dbOtherEngine.get())
				.dbParameterGroupName(parameterGroup)
				.availabilityZone(availabilityZone)
				.dbClusterIdentifier(clusterName)
				.promotionTier(rdsSettings.dbOtherPromotionTier.get())
				.build();
		getAmazonRdsCreateOtherClient(region).createDBInstance(request);
	}

	public DescribeBlueGreenDeploymentsResponse getBlueGreenDeployment(String blueGreenDeploymentId, String region){
		var request = DescribeBlueGreenDeploymentsRequest.builder()
				.blueGreenDeploymentIdentifier(blueGreenDeploymentId)
				.build();
		return getAmazonRdsReadOnlyClient(region).describeBlueGreenDeployments(request);
	}

	public String getClusterParameterGroup(String clusterName, String region){
		return getCluster(clusterName, region).dbClusterParameterGroup();
	}

	public String getClusterFromInstanceName(String instanceName, String region){
		return getInstance(instanceName, region).dbClusterIdentifier();
	}

	public DBCluster getCluster(String clusterName, String region){
		var request = DescribeDbClustersRequest.builder()
				.dbClusterIdentifier(clusterName)
				.build();
		int randomSleepMs = RandomTool.getRandomIntBetweenTwoNumbers(0, 3_000);
		List<DBCluster> result = RetryableTool.tryNTimesWithBackoffAndRandomInitialDelayUnchecked(
				() -> getAmazonRdsReadOnlyClient(region).describeDBClusters(request).dbClusters(),
				NUM_ATTEMPTS,
				randomSleepMs,
				true);
		if(result.size() > 1){
			throw new RuntimeException(result.size() + " clusters found for " + clusterName);
		}
		return result.getFirst();
	}

	public ListTagsForResourceResponse getTags(String instance, String region){
		String instanceArn = getInstance(instance, region).dbInstanceArn();
		ListTagsForResourceRequest listTagsRequest = ListTagsForResourceRequest.builder()
				.resourceName(instanceArn)
				.build();
		return getAmazonRdsReadOnlyClient(region).listTagsForResource(listTagsRequest);
	}

	public void applyMissingTags(String instanceName, List<Tag> missingTags, String region){
		AddTagsToResourceRequest addTagsRequest = AddTagsToResourceRequest.builder()
				.resourceName(getInstanceArn(instanceName, region))
				.tags(missingTags)
				.build();
		getAmazonRdsAddTagsClient(region).addTagsToResource(addTagsRequest);
	}

	private String getInstanceArn(String instanceName, String region){
		return getInstance(instanceName, region).dbInstanceArn();
	}

	private RdsClient getAmazonRdsReadOnlyClient(String region){
		RdsCredentialsDto credentialsDto = rdsSettings.rdsReadOnlyCredentials.get();
		AwsCredentials credentials = AwsBasicCredentials.create(credentialsDto.accessKey(),
				credentialsDto.secretKey());
		return RdsClient.builder()
				.credentialsProvider(StaticCredentialsProvider.create(credentials))
				.region(Region.of(region))
				.build();
	}

	private RdsClient getAmazonRdsAddTagsClient(String region){
		RdsCredentialsDto credentialsDto = rdsSettings.rdsAddTagsCredentials.get();
		AwsCredentials awsCredentials = AwsBasicCredentials.create(credentialsDto.accessKey(),
				credentialsDto.secretKey());
		return RdsClient.builder()
				.credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
				.region(Region.of(region))
				.build();
	}

	//get RDS client for su_rdsdbothers IAM user
	private RdsClient getAmazonRdsCreateOtherClient(String region){
		RdsCredentialsDto credentialsDto = rdsSettings.rdsOtherCredentials.get();
		AwsCredentials awsCredentials = AwsBasicCredentials.create(credentialsDto.accessKey(),
				credentialsDto.secretKey());
		return RdsClient.builder()
				.credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
				.region(Region.of(region))
				.build();
	}

}
