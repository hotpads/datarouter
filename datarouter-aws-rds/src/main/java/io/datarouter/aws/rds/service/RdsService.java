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
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.AmazonRDSClientBuilder;
import com.amazonaws.services.rds.model.AddTagsToResourceRequest;
import com.amazonaws.services.rds.model.CreateDBInstanceRequest;
import com.amazonaws.services.rds.model.DBCluster;
import com.amazonaws.services.rds.model.DBClusterMember;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DeleteDBInstanceRequest;
import com.amazonaws.services.rds.model.DescribeDBClustersRequest;
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest;
import com.amazonaws.services.rds.model.ListTagsForResourceRequest;
import com.amazonaws.services.rds.model.ListTagsForResourceResult;
import com.amazonaws.services.rds.model.Tag;

import io.datarouter.aws.rds.config.DatarouterAwsRdsConfigSettings;
import io.datarouter.aws.rds.config.DatarouterAwsRdsConfigSettings.RdsCredentialsDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.number.RandomTool;
import io.datarouter.util.retry.RetryableTool;

@Singleton
public class RdsService{

	private static final int NUM_ATTEMPTS = 5;
	private static final String AVAILABLE_STATUS = "available";

	@Inject
	private DatarouterAwsRdsConfigSettings rdsSettings;

	public List<String> getReaderInstanceIds(String clusterName, String region){
		return getCluster(clusterName, region).getDBClusterMembers().stream()
				.filter(m -> !m.isClusterWriter())
				.map(DBClusterMember::getDBInstanceIdentifier)
				.collect(Collectors.toList());
	}

	public boolean isReaderInstance(String instanceName, String clusterName, String region){
		return Scanner.of(getCluster(clusterName, region).getDBClusterMembers())
				.include(clusterMember -> !clusterMember.isClusterWriter())
				.map(DBClusterMember::getDBInstanceIdentifier)
				.anyMatch(instanceId -> instanceId.equals(instanceName));
	}

	public DBInstance getInstance(String instanceName, String region){
		var request = new DescribeDBInstancesRequest().withDBInstanceIdentifier(instanceName);
		int randomSleepMs = RandomTool.getRandomIntBetweenTwoNumbers(0, 3_000);
		return RetryableTool.tryNTimesWithBackoffAndRandomInitialDelayUnchecked(
				() -> getAmazonRdsReadOnlyClient(region).describeDBInstances(request).getDBInstances().get(0),
				NUM_ATTEMPTS,
				randomSleepMs,
				true);
	}

	public DBInstance getWriterInstance(String clusterName, String region){
		Optional<String> writerInstance = Scanner.of(getCluster(clusterName, region).getDBClusterMembers())
				.include(DBClusterMember::isClusterWriter)
				.map(DBClusterMember::getDBInstanceIdentifier)
				.findFirst();
		return getInstance(writerInstance.get(), region);
	}

	public void createOtherInstance(String clusterName, String region){
		String otherInstanceName = clusterName + rdsSettings.dbOtherInstanceSuffix.get();
		if(!getReaderInstanceIds(clusterName, region).contains(otherInstanceName)){
			String availabilityZone = getWriterInstance(clusterName, region).getAvailabilityZone();
			createDbInstance(otherInstanceName, clusterName, region, availabilityZone);
		}
	}

	public void deleteOtherInstance(String instanceName, String region){
		var describeRequest = new DescribeDBInstancesRequest().withDBInstanceIdentifier(instanceName);
		String instanceStatus = getAmazonRdsCreateOtherClient(region).describeDBInstances(describeRequest)
				.getDBInstances().get(0)
				.getDBInstanceStatus();
		if(instanceStatus.equals(AVAILABLE_STATUS) && instanceName.endsWith(
				rdsSettings.dbOtherInstanceSuffix.get())){
			var request = new DeleteDBInstanceRequest()
					.withDBInstanceIdentifier(instanceName);
			getAmazonRdsCreateOtherClient(region).deleteDBInstance(request);
		}
	}

	public void createDbInstance(String instanceName, String clusterName, String region, String availabilityZone){
		String parameterGroup = rdsSettings.getParameterGroup(region);
		var request = new CreateDBInstanceRequest()
				.withDBInstanceIdentifier(instanceName)
				.withDBInstanceClass(rdsSettings.dbOtherInstanceClass.get())
				.withEngine(rdsSettings.dbOtherEngine.get())
				.withDBParameterGroupName(parameterGroup)
				.withAvailabilityZone(availabilityZone)
				.withDBClusterIdentifier(clusterName);

		request.setPromotionTier(rdsSettings.dbOtherPromotionTier.get());
		getAmazonRdsCreateOtherClient(region).createDBInstance(request);
	}

	public String getClusterParameterGroup(String clusterName, String region){
		return getCluster(clusterName, region).getDBClusterParameterGroup();
	}

	public String getClusterFromInstanceName(String instanceName, String region){
		return getInstance(instanceName, region).getDBClusterIdentifier();
	}

	public DBCluster getCluster(String clusterName, String region){
		var request = new DescribeDBClustersRequest().withDBClusterIdentifier(clusterName);
		int randomSleepMs = RandomTool.getRandomIntBetweenTwoNumbers(0, 3_000);
		List<DBCluster> result = RetryableTool.tryNTimesWithBackoffAndRandomInitialDelayUnchecked(
				() -> getAmazonRdsReadOnlyClient(region).describeDBClusters(request).getDBClusters(),
				NUM_ATTEMPTS,
				randomSleepMs,
				true);
		if(result.size() > 1){
			throw new RuntimeException(result.size() + " clusters found for " + clusterName);
		}
		return result.get(0);
	}

	public ListTagsForResourceResult getTags(String instance, String region){
		String instanceArn = getInstance(instance, region).getDBInstanceArn();
		ListTagsForResourceRequest listTagsRequest = new ListTagsForResourceRequest().withResourceName(instanceArn);
		return getAmazonRdsReadOnlyClient(region).listTagsForResource(listTagsRequest);
	}

	public void applyMissingTags(String instanceName, List<Tag> missingTags, String region){
		AddTagsToResourceRequest addTagsRequest = new AddTagsToResourceRequest()
				.withResourceName(getInstanceArn(instanceName, region));
		addTagsRequest.setTags(missingTags);
		getAmazonRdsAddTagsClient(region).addTagsToResource(addTagsRequest);
	}

	private String getInstanceArn(String instanceName, String region){
		return getInstance(instanceName, region).getDBInstanceArn();
	}

	private AmazonRDS getAmazonRdsReadOnlyClient(String region){
		RdsCredentialsDto credentialsDto = rdsSettings.rdsReadOnlyCredentials.get();
		AWSCredentials credentials = new BasicAWSCredentials(credentialsDto.accessKey(), credentialsDto.secretKey());
		return AmazonRDSClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withRegion(region)
				.build();
	}

	private AmazonRDS getAmazonRdsAddTagsClient(String region){
		RdsCredentialsDto credentialsDto = rdsSettings.rdsAddTagsCredentials.get();
		AWSCredentials awsCredentials = new BasicAWSCredentials(credentialsDto.accessKey(), credentialsDto.secretKey());
		return AmazonRDSClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
				.withRegion(region)
				.build();
	}

	//get RDS client for su_rdsdbothers IAM user
	private AmazonRDS getAmazonRdsCreateOtherClient(String region){
		RdsCredentialsDto credentialsDto = rdsSettings.rdsOtherCredentials.get();
		AWSCredentials awsCredentials = new BasicAWSCredentials(credentialsDto.accessKey(), credentialsDto.secretKey());
		return AmazonRDSClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
				.withRegion(region)
				.build();
	}

}
