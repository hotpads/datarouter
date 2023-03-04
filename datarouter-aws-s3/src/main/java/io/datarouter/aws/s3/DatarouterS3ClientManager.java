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
package io.datarouter.aws.s3;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetBucketLocationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketLocationResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.transfer.s3.S3TransferManager;

@SuppressWarnings("serial")
public class DatarouterS3ClientManager implements Serializable{

	/**
	 * With AWS SDK v2, you need to know the region of the bucket when you make a query to it. This client manages
	 * multiple underlying clients for each region. It will try its best to determine the region of a bucket. To do
	 * this, it uses the getBucketLocation API. This API returns the region of the bucket, if the credentials have
	 * permissions to call it. Otherwise, if called on US-EAST-1, it throws an exception that reveals the region of the
	 * bucket. If called on another region, it throws a 403. Therefore, by parsing the exception, we can determine the
	 * region of all buckets at runtime. We're probably going to persist this information in the future in case their
	 * API changes.
	 */
	public static final Region DEFAULT_REGION = Region.US_EAST_1;
	private static final Pattern EXPECTED_REGION_EXTRACTOR = Pattern.compile("expecting '(.*)'");

	private final SerializableAwsCredentialsProviderProvider<?> awsCredentialsProviderProvider;

	private transient Map<Region,S3Client> s3ClientByRegion;
	private transient Map<String,Region> regionByBucket;
	private transient S3Presigner s3Presigner;
	private transient Map<Region,S3TransferManager> transferManagerByRegion;

	public DatarouterS3ClientManager(SerializableAwsCredentialsProviderProvider<?> awsCredentialsProviderProvider){
		this.awsCredentialsProviderProvider = awsCredentialsProviderProvider;
		init();
	}

	/**
	 * Part of the {@link Serializable} interface, sets up transient fields
	 */
	public Object readResolve(){
		init();
		return this;
	}

	private void init(){
		this.s3ClientByRegion = new ConcurrentHashMap<>();
		this.regionByBucket = new ConcurrentHashMap<>();
		this.transferManagerByRegion = new ConcurrentHashMap<>();
		this.s3ClientByRegion.put(DEFAULT_REGION, createClient(DEFAULT_REGION));
		this.s3Presigner = S3Presigner.builder()
				.credentialsProvider(awsCredentialsProviderProvider.get())
				.region(DEFAULT_REGION)
				.build();
	}

	public S3Presigner getPresigner(){
		return s3Presigner;
	}

	public Region getCachedOrLatestRegionForBucket(String bucket){
		return regionByBucket.computeIfAbsent(bucket, this::getBucketRegion);
	}

	public S3TransferManager getTransferManagerForBucket(String bucket){
		Region region = regionByBucket.computeIfAbsent(bucket, this::getBucketRegion);
		return transferManagerByRegion.computeIfAbsent(region, this::createTransferManager);
	}

	public S3Client getS3ClientForRegion(Region region){
		return s3ClientByRegion.computeIfAbsent(region, this::createClient);
	}

	public S3Client getS3ClientForBucket(String bucket){
		Region region = regionByBucket.computeIfAbsent(bucket, this::getBucketRegion);
		return getS3ClientForRegion(region);
	}

	public Region getBucketRegion(String bucket){
		String region;
		S3Client s3Client = s3ClientByRegion.get(DEFAULT_REGION);
		try{
			GetBucketLocationRequest request = GetBucketLocationRequest.builder()
					.bucket(bucket)
					.build();
			GetBucketLocationResponse response;
			try(var $ = TracerTool.startSpan("S3 getBucketLocation", TraceSpanGroupType.CLOUD_STORAGE)){
				response = s3Client.getBucketLocation(request);
			}
			region = response.locationConstraintAsString();
		}catch(NoSuchBucketException e){
			throw new RuntimeException("bucket not found name=" + bucket, e);
		}catch(S3Exception e){
			Matcher matcher = EXPECTED_REGION_EXTRACTOR.matcher(e.getMessage());
			if(matcher.find()){
				region = matcher.group(1);
			}else{
				try{
					HeadBucketRequest request = HeadBucketRequest.builder()
							.bucket(bucket)
							.build();
					HeadBucketResponse response;
					try(var $ = TracerTool.startSpan("S3 headBucket", TraceSpanGroupType.CLOUD_STORAGE)){
						response = s3Client.headBucket(request);
					}
					region = response
							.sdkHttpResponse()
							.firstMatchingHeader(S3Headers.BUCKET_REGION)
							.get();
				}catch(S3Exception e2){
					region = e2.awsErrorDetails().sdkHttpResponse().firstMatchingHeader(S3Headers.BUCKET_REGION).get();
				}
			}
		}
		return region.isEmpty() ? DEFAULT_REGION : Region.of(region);
	}

	private S3Client createClient(Region region){
		return S3Client.builder()
				.credentialsProvider(awsCredentialsProviderProvider.get())
				.region(region)
				.httpClientBuilder(ApacheHttpClient.builder().maxConnections(50_000))
				.build();
	}

	private S3TransferManager createTransferManager(Region region){
		return S3TransferManager.builder()
				.s3Client(S3AsyncClient.crtBuilder()
						.credentialsProvider(awsCredentialsProviderProvider.get())
						.region(region)
						.build())
				.build();
	}

}
