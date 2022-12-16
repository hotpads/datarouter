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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import io.datarouter.aws.s3.S3Headers.ContentType;
import io.datarouter.aws.s3.S3Headers.S3ContentType;
import io.datarouter.bytes.split.ChunkScannerTool;
import io.datarouter.scanner.ParallelScannerContext;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.node.op.raw.read.DirectoryDto;
import io.datarouter.util.io.ReaderTool;
import io.datarouter.util.tuple.Range;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.S3Object;

public interface DatarouterS3Client{

	/*--------- buckets ---------*/

	Scanner<Bucket> scanBuckets();

	Region getBucketRegion(String bucket);

	Region getCachedOrLatestRegionForBucket(String bucketName);

	/*--------- objects ---------*/

	Optional<HeadObjectResponse> headObject(String bucket, String key);

	default boolean exists(String bucket, String key){
		return headObject(bucket, key).isPresent();
	}

	default boolean existsPrefix(String bucket, String prefix){
		return scanObjects(bucket, prefix).hasAny();
	}

	default Optional<Long> length(String bucket, String key){
		return headObject(bucket, key)
				.map(HeadObjectResponse::contentLength);
	}

	default Optional<Instant> findLastModified(String bucket, String key){
		return headObject(bucket, key)
				.map(HeadObjectResponse::lastModified);
	}

	default Optional<S3Object> findLastModifiedObjectWithPrefix(String bucket, String prefix){
		return scanObjects(bucket, prefix)
				.findMax(Comparator.comparing(S3Object::lastModified));
	}

	void deleteObject(String bucket, String key);

	void deleteObjects(String bucket, Collection<String> keys);

	Scanner<List<S3Object>> scanObjectsPaged(String bucket, String prefix);

	default Scanner<S3Object> scanObjects(String bucket, String prefix){
		return scanObjectsPaged(bucket, prefix)
				.concat(Scanner::of);
	}

	Scanner<S3Object> scanObjects(String bucket, String prefix, String startAfter, String delimiter);

	Scanner<String> scanPrefixes(String bucket, String prefix, String startAfter, String delimiter);

	List<String> getCommonPrefixes(String bucket, String prefix, String delimiter);

	Scanner<DirectoryDto> scanSubdirectories(
			String bucket,
			String prefix,
			String startAfter,
			String delimiter,
			int pageSize,
			boolean currentDirectory);

	URL generateLink(String bucket, String key, Duration expireAfter);

	/*---------- data read bytes ---------*/

	ResponseInputStream<GetObjectResponse> getObjectResponse(String bucket, String key);

	InputStream getObject(String bucket, String key);

	byte[] getObjectAsBytes(String bucket, String key);

	byte[] getPartialObject(String bucket, String key, long offset, int length);

	default Scanner<byte[]> scanObjectChunks(
			String bucket,
			String key,
			Range<Long> range,
			ExecutorService exec,
			int numThreads,
			int chunkSize){
		long fromInclusive = range.hasStart() ? range.getStart() : 0;
		long toExclusive = range.hasEnd()
				? range.getEnd()
				: length(bucket, key).orElseThrow();// extra operation
		return ChunkScannerTool.scanChunks(fromInclusive, toExclusive, chunkSize)
				.parallel(new ParallelScannerContext(exec, numThreads, false))
				.map(chunkRange -> getPartialObject(bucket, key, chunkRange.start, chunkRange.length));
	}

	/*---------- data read files -------*/

	default void downloadFilesToDirectory(String bucket, String prefix, Path path){
		scanObjects(bucket, prefix)
				.map(S3Object::key)
				.forEach(key -> downloadFileToDirectory(bucket, key, path));
	}

	Path downloadFileToDirectory(String bucket, String key, Path path);

	void downloadFile(String bucket, String key, Path path);

	void downloadFileWithHeartbeat(String bucket, String key, Path path, Runnable heartbeat);

	/*---------- data read strings -------*/

	default Scanner<List<String>> scanBatchesOfLinesWithPrefix(String bucket, String prefix, int batchSize){
		return scanObjects(bucket, prefix)
				.map(S3Object::key)
				.concat(key -> scanBatchesOfLines(bucket, key, batchSize));
	}

	default Scanner<String> scanLines(String bucket, String key){
		return ReaderTool.lines(new BufferedReader(new InputStreamReader(getObject(bucket, key))));
	}

	default Scanner<List<String>> scanBatchesOfLines(String bucket, String key, int batchSize){
		return scanLines(bucket, key)
				.batch(batchSize);
	}

	default String getObjectAsString(String bucket, String key){
		return new String(getObjectAsBytes(bucket, key));
	}

	/*---------- data write bytes ---------*/

	void copyObject(String bucket, String sourceKey, String destinationKey, ObjectCannedACL acl);

	OutputStream putWithPublicRead(String bucket, String key, S3ContentType contentType);

	OutputStream put(String bucket, String key, S3ContentType contentType);

	default void put(String bucket, String key, S3ContentType contentType, InputStream inputStream){
		multipartUpload(bucket, key, contentType, inputStream);
	}

	void putObjectAsBytes(
			String bucket,
			String key,
			ContentType contentType,
			String cacheControl,
			ObjectCannedACL acl,
			byte[] bytes);

	void putObjectAsBytesWithExpirationTime(
			String bucket,
			String key,
			ContentType contentType,
			String cacheControl,
			ObjectCannedACL acl,
			byte[] bytes,
			Instant expirationTime);

	//TODO rename multiPartUpload
	void multipartUpload(String bucket, String key, S3ContentType contentType, InputStream inputStream);

	void multiThreadUpload(
			String bucket,
			String key,
			S3ContentType contentType,
			InputStream inputStream,
			ExecutorService exec,
			int numThreads);

	/*----------- data write files ----------*/

	void putObjectWithHeartbeat(String bucket, String key, ContentType contentType, Path path, Runnable heartbeat);

	void putPublicObject(String bucket, String key, ContentType contentType, Path path);

	void putObject(String bucket, String key, ContentType contentType, Path path);

	/*---------- data write strings ---------*/

	void putObjectAsString(String bucket, String key, ContentType contentType, String content);

	default BufferedWriter putAsWriter(String bucket, String key, ContentType contentType){
		return new BufferedWriter(new OutputStreamWriter(put(bucket, key, contentType)));
	}

}
