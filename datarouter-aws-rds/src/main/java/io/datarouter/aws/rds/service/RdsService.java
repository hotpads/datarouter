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
package io.datarouter.aws.rds.service;

import java.util.List;
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
import com.amazonaws.services.rds.model.DescribeDBClustersRequest;
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest;
import com.amazonaws.services.rds.model.ListTagsForResourceRequest;
import com.amazonaws.services.rds.model.ListTagsForResourceResult;
import com.amazonaws.services.rds.model.Tag;

import io.datarouter.aws.rds.config.DatarouterAwsRdsConfigSettings;
import io.datarouter.aws.rds.config.DatarouterAwsRdsConfigSettings.RdsCredentialsDto;
import io.datarouter.util.number.RandomTool;
import io.datarouter.util.retry.RetryableTool;

@Singleton
public class RdsService{

	private static final int NUM_ATTEMPTS = 3;

	@Inject
	private DatarouterAwsRdsConfigSettings rdsSettings;

	public List<String> getReaderInstanceIds(String clusterName){
		return getCluster(clusterName).getDBClusterMembers().stream()
				.filter(m -> !m.isClusterWriter())
				.map(DBClusterMember::getDBInstanceIdentifier)
				.collect(Collectors.toList());
	}

	public List<String> getInstanceIds(String clusterName){
		return getCluster(clusterName).getDBClusterMembers().stream()
				.map(DBClusterMember::getDBInstanceIdentifier)
				.collect(Collectors.toList());
	}

	public DBInstance getInstance(String instanceName){
		var request = new DescribeDBInstancesRequest().withDBInstanceIdentifier(instanceName);
		int randomSleepMs = RandomTool.getRandomIntBetweenTwoNumbers(0, 3_000);
		return RetryableTool.tryNTimesWithBackoffUnchecked(
				() -> getAmazonRdsReadOnlyClient().describeDBInstances(request).getDBInstances().get(0),
				NUM_ATTEMPTS,
				randomSleepMs,
				true);
	}

	public void createOtherInstance(String clusterName){
		String otherInstanceName = clusterName + rdsSettings.dbOtherInstanceSuffix.get();
		if(!getReaderInstanceIds(clusterName).contains(otherInstanceName)){
			createDbInstance(otherInstanceName, clusterName);
		}
	}

	public void createDbInstance(String instanceName, String clusterName){
		var request = new CreateDBInstanceRequest()
				.withDBInstanceIdentifier(instanceName)
				.withDBInstanceClass(rdsSettings.dbOtherInstanceClass.get())
				.withEngine(rdsSettings.dbOtherEngine.get())
				.withDBParameterGroupName(rdsSettings.dbOtherParameterGroup.get())
				.withDBClusterIdentifier(clusterName);

		request.setPromotionTier(rdsSettings.dbOtherPromotionTier.get());
		getAmazonRdsCreateOtherClient().createDBInstance(request);
	}

	public String getClusterParameterGroup(String clusterName){
		return getCluster(clusterName).getDBClusterParameterGroup();
	}

	public String getClusterFromInstanceName(String instanceName){
		return getInstance(instanceName).getDBClusterIdentifier();
	}

	public DBCluster getCluster(String clusterName){
		var request = new DescribeDBClustersRequest().withDBClusterIdentifier(clusterName);
		int randomSleepMs = RandomTool.getRandomIntBetweenTwoNumbers(0, 3_000);
		List<DBCluster> result = RetryableTool.tryNTimesWithBackoffUnchecked(
				() -> getAmazonRdsReadOnlyClient().describeDBClusters(request).getDBClusters(),
				NUM_ATTEMPTS,
				randomSleepMs,
				true);
		if(result.size() > 1){
			throw new RuntimeException(result.size() + " clusters found for " + clusterName);
		}
		return result.get(0);
	}

	public List<DBClusterMember> getClusterMembers(String clusterName){
		return getCluster(clusterName).getDBClusterMembers();
	}

	public ListTagsForResourceResult getTags(String instance){
		String instanceArn = getInstance(instance).getDBInstanceArn();
		ListTagsForResourceRequest listTagsRequest = new ListTagsForResourceRequest().withResourceName(instanceArn);
		return getAmazonRdsReadOnlyClient().listTagsForResource(listTagsRequest);
	}

	public void applyMissingTags(String instanceName, List<Tag> missingTags){
		AddTagsToResourceRequest addTagsRequest = new AddTagsToResourceRequest()
				.withResourceName(getInstanceArn(instanceName));
		addTagsRequest.setTags(missingTags);
		getAmazonRdsAddTagsClient().addTagsToResource(addTagsRequest);
	}

	private String getInstanceArn(String instanceName){
		return getInstance(instanceName).getDBInstanceArn();
	}

	private AmazonRDS getAmazonRdsReadOnlyClient(){
		RdsCredentialsDto credentialsDto = rdsSettings.rdsReadOnlyCredentials.get();
		AWSCredentials credentials = new BasicAWSCredentials(credentialsDto.accessKey, credentialsDto.secretKey);
		return AmazonRDSClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withRegion(rdsSettings.region.get())
				.build();
	}

	private AmazonRDS getAmazonRdsAddTagsClient(){
		RdsCredentialsDto credentialsDto = rdsSettings.rdsAddTagsCredentials.get();
		AWSCredentials awsCredentials = new BasicAWSCredentials(credentialsDto.accessKey, credentialsDto.secretKey);
		return AmazonRDSClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
				.withRegion(rdsSettings.region.get())
				.build();
	}

	//get RDS client for su_rdsdbothers IAM user
	private AmazonRDS getAmazonRdsCreateOtherClient(){
		AWSCredentials awsCredentials2 = new BasicAWSCredentials(
				rdsSettings.iamRdsOtherCreateUserAccessKey.get(),
				rdsSettings.iamRdsOtherCreateUserSecretKey.get());
		return AmazonRDSClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(awsCredentials2))
				.withRegion(rdsSettings.region.get())
				.build();
	}

}
