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
package io.datarouter.aws.s3.request;

import java.util.List;
import java.util.Optional;

import io.datarouter.aws.s3.DatarouterS3ClientManager;
import io.datarouter.aws.s3.DatarouterS3Counters;
import io.datarouter.aws.s3.DatarouterS3Counters.S3CounterSuffix;
import io.datarouter.aws.s3.S3CostCounters;
import io.datarouter.aws.s3.S3ListVersionsResponse;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.file.BucketAndKeyVersion;
import io.datarouter.storage.file.BucketAndKeyVersionResult;
import io.datarouter.storage.file.BucketAndKeyVersions;
import io.datarouter.storage.file.BucketAndPrefix;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsResponse;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.paginators.ListObjectVersionsIterable;

public class DatarouterS3VersionRequests{

	private final DatarouterS3ClientManager clientManager;

	public DatarouterS3VersionRequests(DatarouterS3ClientManager clientManager){
		this.clientManager = clientManager;
	}

	/*--------- scan---------*/

	public Scanner<List<BucketAndKeyVersionResult>> scanVersionsPaged(
			BucketAndPrefix location,
			int pageSize){
		DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.SCAN_VERSIONS_SCANS, 1);
		ListObjectVersionsRequest listObjectVersionsRequest = ListObjectVersionsRequest.builder()
				.bucket(location.bucket())
				.prefix(location.prefix())
				.maxKeys(pageSize)
				.build();
		ListObjectVersionsIterable listObjectVersionsIterable = clientManager.getS3ClientForBucket(location.bucket())
				.listObjectVersionsPaginator(listObjectVersionsRequest);
		return Scanner.of(listObjectVersionsIterable)
				.timeNanos(nanos -> TracerTool.addSpan(
						"S3 listObjectVersions page",
						TraceSpanGroupType.CLOUD_STORAGE,
						System.nanoTime(),
						nanos))
				.map(response -> new S3ListVersionsResponse(
						location.bucket(),
						response.versions(),
						response.deleteMarkers()))
				.map(S3ListVersionsResponse::list)
				.each(page -> {
					DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.LIST_VERSIONS_REQUESTS, 1);
					DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.LIST_VERSIONS_ROWS, page.size());
					S3CostCounters.list();
				});
	}

	public Scanner<List<BucketAndKeyVersionResult>> scanVersionsFromPaged(
			BucketAndPrefix location,
			String from,
			int pageSize){
		DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.SCAN_VERSIONS_FROM_SCANS, 1);
		ListObjectVersionsRequest listObjectVersionsRequest = ListObjectVersionsRequest.builder()
				.bucket(location.bucket())
				.prefix(location.prefix())
				.keyMarker(from)
				.maxKeys(pageSize)
				.build();
		ListObjectVersionsIterable listObjectVersionsIterable = clientManager.getS3ClientForBucket(location.bucket())
				.listObjectVersionsPaginator(listObjectVersionsRequest);
		return Scanner.of(listObjectVersionsIterable)
				.timeNanos(nanos -> TracerTool.addSpan(
						"S3 listObjectVersions page",
						TraceSpanGroupType.CLOUD_STORAGE,
						System.nanoTime(),
						nanos))
				.map(response -> new S3ListVersionsResponse(
						location.bucket(),
						response.versions(),
						response.deleteMarkers()))
				.map(S3ListVersionsResponse::list)
				.each(page -> {
					DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.LIST_VERSIONS_REQUESTS, 1);
					DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.LIST_VERSIONS_ROWS, page.size());
					S3CostCounters.list();
				});
	}

	public Scanner<String> scanVersionPrefixes(BucketAndPrefix locationPrefix, String delimiter){
		var requestBuilder = ListObjectVersionsRequest.builder()
				.bucket(locationPrefix.bucket());
		Optional.ofNullable(locationPrefix.prefix()).ifPresent(requestBuilder::prefix);
		Optional.ofNullable(delimiter).ifPresent(requestBuilder::delimiter);
		ListObjectVersionsIterable listObjectVersionsIterable = clientManager.getS3ClientForBucket(
				locationPrefix.bucket())
				.listObjectVersionsPaginator(requestBuilder.build());
		return Scanner.of(listObjectVersionsIterable)
				.timeNanos(nanos -> TracerTool.addSpan(
						"S3 listObjectVersions page",
						TraceSpanGroupType.CLOUD_STORAGE,
						System.nanoTime(),
						nanos))
				.concatIter(ListObjectVersionsResponse::commonPrefixes)
				.map(CommonPrefix::prefix);
	}

	/*---------- common prefixes ----------*/

	public boolean hasVersionCommonPrefixes(BucketAndPrefix locationPrefix, String delimiter){
		List<String> commonPrefixes = getVersionCommonPrefixes(locationPrefix, delimiter, 1);
		return !commonPrefixes.isEmpty();
	}

	public List<String> getVersionCommonPrefixes(BucketAndPrefix locationPrefix, String delimiter){
		return getVersionCommonPrefixes(locationPrefix, delimiter, 1_000);
	}

	private List<String> getVersionCommonPrefixes(BucketAndPrefix locationPrefix, String delimiter, int maxKeys){
		S3Client s3Client = clientManager.getS3ClientForBucket(locationPrefix.bucket());
		ListObjectVersionsRequest request = ListObjectVersionsRequest.builder()
				.bucket(locationPrefix.bucket())
				.prefix(locationPrefix.prefix())
				.delimiter(delimiter)
				.maxKeys(maxKeys)
				.build();
		ListObjectVersionsResponse response;
		try(var $ = TracerTool.startSpan("S3 listObjectsVersions commonPrefixes", TraceSpanGroupType.CLOUD_STORAGE)){
			response = s3Client.listObjectVersions(request);
			TracerTool.appendToSpanInfo("size", response.commonPrefixes().size());
		}
		return response
				.commonPrefixes()
				.stream()
				.map(CommonPrefix::prefix)
				.toList();
	}

	/*---------- delete ----------*/

	public void deleteVersion(BucketAndKeyVersion bucketAndKeyVersion){
		DeleteObjectRequest request = DeleteObjectRequest.builder()
				.bucket(bucketAndKeyVersion.bucket())
				.key(bucketAndKeyVersion.key())
				.versionId(bucketAndKeyVersion.version())
				.build();
		S3Client s3Client = clientManager.getS3ClientForBucket(bucketAndKeyVersion.bucket());
		try(var $ = TracerTool.startSpan("S3 deleteVersion", TraceSpanGroupType.CLOUD_STORAGE)){
			s3Client.deleteObject(request);
		}
		DatarouterS3Counters.inc(bucketAndKeyVersion.bucket(), S3CounterSuffix.DELETE_VERSION_REQUESTS, 1);
		// Cost: S3 deletes are free
	}

	public void deleteVersions(BucketAndKeyVersions bucketAndKeyVersions){
		//TODO batch in case the caller provides a request over the limit
		List<ObjectIdentifier> objectIdentifiers = Scanner.of(bucketAndKeyVersions.keyVersions())
				.map(keyVersion -> ObjectIdentifier.builder()
						.key(keyVersion.key())
						.versionId(keyVersion.version())
						.build())
				.list();
		DeleteObjectsRequest request = DeleteObjectsRequest.builder()
				.bucket(bucketAndKeyVersions.bucket())
				.delete(Delete.builder().objects(objectIdentifiers).build())
				.build();
		S3Client s3Client = clientManager.getS3ClientForBucket(bucketAndKeyVersions.bucket());
		try(var $ = TracerTool.startSpan("S3 deleteVersions", TraceSpanGroupType.CLOUD_STORAGE)){
			s3Client.deleteObjects(request);
		}
		DatarouterS3Counters.inc(
				bucketAndKeyVersions.bucket(),
				S3CounterSuffix.DELETE_VERSIONS_REQUESTS,
				1);
		DatarouterS3Counters.inc(
				bucketAndKeyVersions.bucket(),
				S3CounterSuffix.DELETE_VERSIONS_KEYS,
				bucketAndKeyVersions.keyVersions().size());
		// Cost: S3 deletes are free
	}

}
