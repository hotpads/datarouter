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
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.file.BucketAndPrefix;
import io.datarouter.storage.node.op.raw.read.DirectoryDto;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

public class DatarouterS3BucketRequests{

	private final DatarouterS3ClientManager clientManager;

	public DatarouterS3BucketRequests(DatarouterS3ClientManager clientManager){
		this.clientManager = clientManager;
	}

	/*--------- buckets ---------*/

	public Scanner<Bucket> scanBuckets(){
		DatarouterS3Counters.incNoBucket(S3CounterSuffix.SCAN_BUCKETS_SCANS, 1);
		S3Client client = clientManager.getS3ClientForRegion(DatarouterS3ClientManager.DEFAULT_REGION);
		List<Bucket> buckets = client.listBuckets().buckets();
		DatarouterS3Counters.incNoBucket(S3CounterSuffix.LIST_BUCKETS_REQUESTS, 1);
		DatarouterS3Counters.incNoBucket(S3CounterSuffix.LIST_BUCKETS_ROWS, buckets.size());
		return Scanner.of(buckets);
	}

	/*--------- object scan---------*/

	public Scanner<List<S3Object>> scanPaged(BucketAndPrefix location, int pageSize){
		DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.SCAN_OBJECTS_SCANS, 1);
		ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
				.bucket(location.bucket())
				.prefix(location.prefix())
				.maxKeys(pageSize)
				.build();
		ListObjectsV2Iterable listObjectsV2Iterable = clientManager.getS3ClientForBucket(location.bucket())
				.listObjectsV2Paginator(listObjectsV2Request);
		return Scanner.of(listObjectsV2Iterable)
				.timeNanos(nanos -> TracerTool.addSpan(
						"S3 ListObjectsV2 page",
						TraceSpanGroupType.CLOUD_STORAGE,
						System.nanoTime(),
						nanos))
				.map(ListObjectsV2Response::contents)
				.each(page -> {
					DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.LIST_OBJECTS_REQUESTS, 1);
					DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.LIST_OBJECTS_ROWS, page.size());
				});
	}

	// TODO return pages?
	public Scanner<S3Object> scanAfter(BucketAndPrefix location, String startAfter, String delimiter){
		DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.SCAN_OBJECTS_AFTER_SCANS, 1);
		var requestBuilder = ListObjectsV2Request.builder()
				.bucket(location.bucket());
		Optional.ofNullable(location.prefix()).ifPresent(requestBuilder::prefix);
		Optional.ofNullable(startAfter).ifPresent(requestBuilder::startAfter);
		Optional.ofNullable(delimiter).ifPresent(requestBuilder::delimiter);
		ListObjectsV2Iterable responsePages = clientManager.getS3ClientForBucket(location.bucket())
				.listObjectsV2Paginator(requestBuilder.build());
		return Scanner.of(responsePages)
				.map(ListObjectsV2Response::contents)
				.each(page -> {
					DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.LIST_OBJECTS_REQUESTS, 1);
					DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.LIST_OBJECTS_ROWS, page.size());
				})
				.concat(Scanner::of);
	}

	/*--------- object scan prefix---------*/

	public Scanner<String> scanPrefixes(BucketAndPrefix locationPrefix, String startAfter, String delimiter){
		var requestBuilder = ListObjectsV2Request.builder()
				.bucket(locationPrefix.bucket());
		Optional.ofNullable(locationPrefix.prefix()).ifPresent(requestBuilder::prefix);
		Optional.ofNullable(startAfter).ifPresent(requestBuilder::startAfter);
		Optional.ofNullable(delimiter).ifPresent(requestBuilder::delimiter);
		ListObjectsV2Iterable responsePages = clientManager.getS3ClientForBucket(locationPrefix.bucket())
				.listObjectsV2Paginator(requestBuilder.build());
		return Scanner.of(responsePages)
				.concatIter(ListObjectsV2Response::commonPrefixes)
				.map(CommonPrefix::prefix);
	}

	public boolean hasCommonPrefixes(BucketAndPrefix locationPrefix, String delimiter){
		List<String> commonPrefixes = getCommonPrefixes(locationPrefix, delimiter, 1);
		return !commonPrefixes.isEmpty();
	}

	public List<String> getCommonPrefixes(BucketAndPrefix locationPrefix, String delimiter){
		return getCommonPrefixes(locationPrefix, delimiter, 1_000);
	}

	private List<String> getCommonPrefixes(BucketAndPrefix locationPrefix, String delimiter, int maxKeys){
		S3Client s3Client = clientManager.getS3ClientForBucket(locationPrefix.bucket());
		ListObjectsV2Request request = ListObjectsV2Request.builder()
				.bucket(locationPrefix.bucket())
				.prefix(locationPrefix.prefix())
				.delimiter(delimiter)
				.maxKeys(maxKeys)
				.build();
		ListObjectsV2Response response;
		try(var $ = TracerTool.startSpan("S3 listObjectsV2", TraceSpanGroupType.CLOUD_STORAGE)){
			response = s3Client.listObjectsV2(request);
			TracerTool.appendToSpanInfo("size", response.contents().size());
		}
		return response
				.commonPrefixes()
				.stream()
				.map(CommonPrefix::prefix)
				.toList();
	}

	public Scanner<DirectoryDto> scanSubdirectories(
			BucketAndPrefix locationPrefix,
			String startAfter,
			String delimiter,
			int pageSize,
			boolean currentDirectory){
		ListObjectsV2Request request = ListObjectsV2Request.builder()
				.bucket(locationPrefix.bucket())
				.prefix(locationPrefix.prefix())
				.startAfter(startAfter)
				.delimiter(delimiter)
				.maxKeys(pageSize)
				.build();
		ListObjectsV2Iterable pages;
		try(var $ = TracerTool.startSpan("S3 listObjectsV2Paginator", TraceSpanGroupType.CLOUD_STORAGE)){
			pages = clientManager.getS3ClientForBucket(locationPrefix.bucket())
					.listObjectsV2Paginator(request);
		}
		return Scanner.of(pages)
				.concat(res -> {
					Scanner<DirectoryDto> objects = Scanner.of(pages)
							.concatIter(ListObjectsV2Response::contents)
							.map(object -> new DirectoryDto(
									object.key(),
									false,
									object.size(),
									object.lastModified(),
									object.storageClass().name()));
					Scanner<DirectoryDto> prefixes = Scanner.of(pages)
							.concatIter(ListObjectsV2Response::commonPrefixes)
							.map(commonPrefix -> new DirectoryDto(
									commonPrefix.prefix(), true, 0L, null, null));
					return Scanner.concat(objects, prefixes);
				});
	}

	public Scanner<DirectoryDto> scanSubdirectoriesOnly(
			BucketAndPrefix locationPrefix,
			String startAfter,
			String delimiter,
			int pageSize){
		ListObjectsV2Request request = ListObjectsV2Request.builder()
				.bucket(locationPrefix.bucket())
				.prefix(locationPrefix.prefix())
				.startAfter(startAfter)
				.delimiter(delimiter)
				.maxKeys(pageSize)
				.build();
		ListObjectsV2Iterable pages;
		try(var $ = TracerTool.startSpan("S3 listObjectsV2Paginator", TraceSpanGroupType.CLOUD_STORAGE)){
			pages = clientManager.getS3ClientForBucket(locationPrefix.bucket()).listObjectsV2Paginator(request);
		}
		return Scanner.of(pages)
				.concatIter(ListObjectsV2Response::commonPrefixes)
				.map(commonPrefix -> new DirectoryDto(commonPrefix.prefix(), true, 0L, null, null));
	}

	public Scanner<DirectoryDto> scanFilesOnly(
			BucketAndPrefix locationPrefix,
			String startAfter,
			String delimiter,
			int pageSize){
		ListObjectsV2Request request = ListObjectsV2Request.builder()
				.bucket(locationPrefix.bucket())
				.prefix(locationPrefix.prefix())
				.startAfter(startAfter)
				.delimiter(delimiter)
				.maxKeys(pageSize)
				.build();
		ListObjectsV2Iterable pages;
		try(var $ = TracerTool.startSpan("S3 listObjectsV2Paginator", TraceSpanGroupType.CLOUD_STORAGE)){
			pages = clientManager.getS3ClientForBucket(locationPrefix.bucket()).listObjectsV2Paginator(request);
		}
		return Scanner.of(pages)
				.concatIter(ListObjectsV2Response::contents)
				.map(object -> new DirectoryDto(
						object.key(),
						false,
						object.size(),
						object.lastModified(),
						object.storageClass().name()));
	}

}
