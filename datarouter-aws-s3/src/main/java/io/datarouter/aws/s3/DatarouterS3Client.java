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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.aws.s3.S3Headers.ContentType;
import io.datarouter.aws.s3.S3Headers.S3ContentType;
import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.io.InputStreamAndLength;
import io.datarouter.bytes.split.ChunkScannerTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.file.BucketAndKey;
import io.datarouter.storage.file.BucketAndKeyVersion;
import io.datarouter.storage.file.BucketAndKeyVersionResult;
import io.datarouter.storage.file.BucketAndKeyVersions;
import io.datarouter.storage.file.BucketAndKeys;
import io.datarouter.storage.file.BucketAndPrefix;
import io.datarouter.storage.node.op.raw.read.DirectoryDto;
import io.datarouter.util.tuple.Range;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.S3Object;

public interface DatarouterS3Client{
	static final Logger logger = LoggerFactory.getLogger(DatarouterS3Client.class);

	static final int DEFAULT_SCAN_PAGE_SIZE = 1_000;// matches S3 default

	/*--------- buckets ---------*/

	Scanner<Bucket> scanBuckets();

	Region getRegionForBucket(String bucket);

	Region getCachedOrLatestRegionForBucket(String bucketName);

	/*--------- object head ---------*/

	Optional<HeadObjectResponse> head(BucketAndKey location);

	default boolean exists(BucketAndKey location){
		return head(location).isPresent();
	}

	default Optional<Long> length(BucketAndKey location){
		return head(location)
				.map(HeadObjectResponse::contentLength);
	}

	default Optional<Instant> findLastModified(BucketAndKey location){
		return head(location)
				.map(HeadObjectResponse::lastModified);
	}

	/*---------- object link ----------*/

	URL generateLink(BucketAndKey location, Duration expireAfter);

	/*--------- object scan---------*/

	Scanner<List<S3Object>> scanPaged(BucketAndPrefix location, int pageSize);

	default Scanner<S3Object> scan(BucketAndPrefix location){
		return scanPaged(location, DEFAULT_SCAN_PAGE_SIZE)
				.concat(Scanner::of);
	}

	/*--------- object scan prefix---------*/

	Scanner<List<S3Object>> scanAfterPaged(BucketAndPrefix location, String startAfter, String delimiter);

	default Scanner<S3Object> scanAfter(BucketAndPrefix location, String startAfter, String delimiter){
		return scanAfterPaged(location, startAfter, delimiter)
				.concat(Scanner::of);
	}

	/**
	 * Avoid calling scanPrefixes on a directory with many files.
	 * It takes forever, even if there are no subdirectories.
	 */
	Scanner<String> scanPrefixes(BucketAndPrefix locationPrefix, String startAfter, String delimiter);

	/**
	 * Returns quickly when there are no subdirectories.
	 */
	boolean hasCommonPrefixes(BucketAndPrefix locationPrefix, String delimiter);

	List<String> getCommonPrefixes(BucketAndPrefix locationPrefix, String delimiter);

	default boolean existsPrefix(BucketAndPrefix locationPrefix){
		return scan(locationPrefix).hasAny();
	}

	default Optional<S3Object> findLastModifiedObjectWithPrefix(BucketAndPrefix locationPrefix){
		return scan(locationPrefix)
				.findMax(Comparator.comparing(S3Object::lastModified));
	}

	Scanner<DirectoryDto> scanSubdirectories(
			BucketAndPrefix locationPrefix,
			String startAfter,
			String delimiter,
			int pageSize,
			boolean currentDirectory);

	Scanner<DirectoryDto> scanFilesOnly(
			BucketAndPrefix locationPrefix,
			String startAfter,
			String delimiter,
			int pageSize);

	Scanner<DirectoryDto> scanSubdirectoriesOnly(
			BucketAndPrefix locationPrefix,
			String startAfter,
			String delimiter,
			int pageSize);

	/*---------- object scan versions --------*/

	Scanner<List<BucketAndKeyVersionResult>> scanVersionsPaged(
			BucketAndPrefix location,
			int pageSize);

	default Scanner<BucketAndKeyVersionResult> scanVersions(BucketAndPrefix bucketAndPrefix){
		return scanVersionsPaged(bucketAndPrefix, DEFAULT_SCAN_PAGE_SIZE)
				.concat(Scanner::of);
	}

	Scanner<List<BucketAndKeyVersionResult>> scanVersionsFromPaged(
			BucketAndPrefix location,
			String after,
			int pageSize);

	default Scanner<BucketAndKeyVersionResult> scanVersionsFrom(
			BucketAndPrefix bucketAndPrefix,
			String from){
		return scanVersionsFromPaged(bucketAndPrefix, from, DEFAULT_SCAN_PAGE_SIZE)
				.concat(Scanner::of);
	}

	Scanner<String> scanVersionPrefixes(BucketAndPrefix bucketAndPrefix, String delimiter);

	boolean hasVersionCommonPrefixes(BucketAndPrefix bucketAndPrefix, String delimiter);

	List<String> getVersionCommonPrefixes(BucketAndPrefix bucketAndPrefix, String delimiter);

	/*---------- object delete ----------*/

	void delete(BucketAndKey location);

	void deleteMulti(BucketAndKeys bucketAndKeys);

	void deleteVersion(BucketAndKeyVersion bucketAndKeyVersion);

	void deleteVersions(BucketAndKeyVersions bucketAndKeyVersions);

	/*---------- data read bytes ---------*/

	ResponseInputStream<GetObjectResponse> getResponseInputStream(BucketAndKey location);

	InputStream getInputStream(BucketAndKey location);

	byte[] getObjectAsBytes(BucketAndKey location);

	byte[] getPartialObject(BucketAndKey location, long offset, int length);

	default Scanner<byte[]> scanObjectChunks(
			BucketAndKey location,
			Range<Long> range,
			Threads threads,
			int chunkSize){
		long fromInclusive = range.hasStart() ? range.getStart() : 0;
		long toExclusive = range.hasEnd()
				? range.getEnd()
				: length(location).orElseThrow();// extra operation
		return ChunkScannerTool.scanChunks(fromInclusive, toExclusive, chunkSize)
				.parallelOrdered(threads)
				.map(chunkRange -> getPartialObject(
						location,
						chunkRange.start,
						chunkRange.length));
	}

	/*---------- data write bytes ---------*/

	void copyObject(String bucket, String sourceKey, String destinationKey, ObjectCannedACL acl);

	void putObject(
			BucketAndKey location,
			S3ContentType contentType,
			byte[] bytes);

	void putObjectWithPublicRead(
			BucketAndKey location,
			ContentType contentType,
			String cacheControl,
			ObjectCannedACL acl,
			byte[] bytes);

	void putObjectWithPublicReadAndExpirationTime(
			BucketAndKey location,
			ContentType contentType,
			String cacheControl,
			byte[] bytes,
			Instant expirationTime);

	/*---------- data write bytes multipart OutputStream ---------*/

	/**
	 * @deprecated  Use the InputStream based methods
	 */
	@Deprecated
	OutputStream multipartUploadOutputStream(
			BucketAndKey location,
			S3ContentType contentType);

	/*---------- data write bytes multipart InputStream ---------*/

	void multipartUpload(
			BucketAndKey location,
			S3ContentType contentType,
			InputStream inputStream);

	void multipartUploadWithPublicRead(
			BucketAndKey location,
			S3ContentType contentType,
			InputStream inputStream);

	void multithreadUpload(
			BucketAndKey location,
			S3ContentType contentType,
			InputStream inputStream,
			Threads threads,
			ByteLength minPartSize);

	/*---------- data write bytes multipart Scanner ---------*/

	/*
	 * Each List<byte[]> becomes an UploadPart.
	 * Implementation can upload the byte[]s within a part without concatenating them.
	 * Caller therefore determines size of parts.
	 */
	void multithreadUpload(
			BucketAndKey location,
			S3ContentType contentType,
			Scanner<InputStreamAndLength> parts,
			Threads threads);

	/*----------- data write bytes multipart sub-ops ----------*/

	String createMultipartUploadRequest(
			BucketAndKey location,
			S3ContentType contentType,
			Optional<ObjectCannedACL> aclOpt);

	CompletedPart uploadPart(
			BucketAndKey location,
			String uploadId,
			int partNumber,
			InputStreamAndLength inputStreamAndLength);

	void completeMultipartUploadRequest(
			BucketAndKey location,
			String uploadId,
			List<CompletedPart> completedParts);

	void abortMultipartUploadRequest(
			BucketAndKey location,
			String uploadId);

	/*---------- object read to local file -------*/

	void downloadToLocalFile(BucketAndKey location, Path localFilePath);

	default void downloadToLocalDirectory(BucketAndPrefix bucketAndPrefix, Path localDirectoryPath){
		scan(bucketAndPrefix)
				.map(S3Object::key)
				.forEach(key -> downloadToLocalFile(
						new BucketAndKey(bucketAndPrefix.bucket(), key),
						localDirectoryPath.resolve(key)));
	}

	/*----------- object write from local file ----------*/

	void uploadLocalFile(BucketAndKey location, ContentType contentType, Path localFilePath);

	void uploadLocalFileWithPublicRead(BucketAndKey location, ContentType contentType, Path localFilePath);

}
