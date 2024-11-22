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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import io.datarouter.aws.s3.S3Headers.ContentType;
import io.datarouter.aws.s3.S3Headers.S3ContentType;
import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.io.InputStreamAndLength;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.file.BucketAndKey;
import io.datarouter.storage.file.BucketAndKeyVersion;
import io.datarouter.storage.file.BucketAndKeyVersionResult;
import io.datarouter.storage.file.BucketAndKeyVersions;
import io.datarouter.storage.file.BucketAndKeys;
import io.datarouter.storage.file.BucketAndPrefix;
import io.datarouter.storage.node.op.raw.read.DirectoryDto;
import jakarta.inject.Singleton;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.S3Object;

@Singleton
public class TestDatarouterS3Client implements DatarouterS3Client{

	private final Path testFolder;

	public TestDatarouterS3Client(){
		try{
			this.testFolder = Files.createTempDirectory(null);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public Path getTestFolder(){
		return testFolder;
	}

	@Override
	public Scanner<Bucket> scanBuckets(){
		throw new UnsupportedOperationException();
	}

	@Override
	public Region getRegionForBucket(String bucket){
		throw new UnsupportedOperationException();
	}

	@Override
	public void copyObject(String sourceBucket, String sourceKey, String destinationBucket, String destinationKey,
			ObjectCannedACL acl){
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(BucketAndKey location){
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteMulti(BucketAndKeys bucketAndKeys){
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteVersion(BucketAndKeyVersion bucketAndKeyVersion){
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteVersions(BucketAndKeyVersions bucketAndKeyVersions){
		throw new UnsupportedOperationException();
	}

	/*--------- multipart upload -----------*/

	/**
	 * @deprecated  Use the InputStream based methods
	 */
	@Deprecated
	@Override
	public OutputStream multipartUploadOutputStream(
			BucketAndKey location,
			S3ContentType contentType){
		throw new UnsupportedOperationException();
	}

	@Override
	public void multipartUpload(
			BucketAndKey location,
			S3ContentType contentType,
			InputStream inputStream){
		throw new UnsupportedOperationException();
	}

	@Override
	public void multipartUploadWithPublicRead(
			BucketAndKey location,
			S3ContentType contentType,
			InputStream inputStream){
		throw new UnsupportedOperationException();
	}

	@Override
	public void multithreadUpload(
			BucketAndKey location,
			S3ContentType contentType,
			InputStream inputStream,
			Threads threads,
			ByteLength minPartSize){
		throw new UnsupportedOperationException();
	}

	@Override
	public void multithreadUpload(
			BucketAndKey location,
			S3ContentType contentType,
			Scanner<InputStreamAndLength> parts,
			Threads threads){
		throw new UnsupportedOperationException();
	}

	@Override
	public String createMultipartUploadRequest(
			BucketAndKey location,
			S3ContentType contentType,
			Optional<ObjectCannedACL> aclOpt){
		throw new UnsupportedOperationException();
	}

	@Override
	public CompletedPart uploadPart(
			BucketAndKey location,
			String uploadId,
			int partNumber,
			InputStreamAndLength inputStreamAndLength){
		throw new UnsupportedOperationException();
	}

	@Override
	public void completeMultipartUploadRequest(
			BucketAndKey location,
			String uploadId,
			List<CompletedPart> completedParts){
		throw new UnsupportedOperationException();
	}

	@Override
	public void abortMultipartUploadRequest(BucketAndKey location, String uploadId){
		throw new UnsupportedOperationException();
	}

	@Override
	public void putObject(
			BucketAndKey location,
			S3ContentType contentType,
			byte[] bytes){
		throw new UnsupportedOperationException();
	}

	@Override
	public void putObjectWithPublicRead(
			BucketAndKey location,
			ContentType contentType,
			String cacheControl,
			ObjectCannedACL acl,
			byte[] bytes){
		throw new UnsupportedOperationException();
	}

	@Override
	public void putObjectWithPublicReadAndExpirationTime(
			BucketAndKey location,
			ContentType contentType,
			String cacheControl,
			byte[] bytes,
			Instant instant){
		throw new UnsupportedOperationException();
	}

	@Override
	public ResponseInputStream<GetObjectResponse> getResponseInputStream(BucketAndKey location){
		throw new UnsupportedOperationException();
	}

	@Override
	public InputStream getInputStream(BucketAndKey location){
		try{
			return Files.newInputStream(testFolder.resolve(Path.of(location.bucket(), location.key())));
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	@Override
	public Optional<byte[]> findObject(BucketAndKey location){
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<byte[]> findPartialObject(BucketAndKey location, long offset, int length){
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<byte[]> findEnding(BucketAndKey location, int length){
		throw new UnsupportedOperationException();
	}

	@Override
	public URL generateLink(BucketAndKey location, Duration expireAfter){
		throw new UnsupportedOperationException();
	}

	@Override
	public Scanner<List<S3Object>> scanPaged(BucketAndPrefix location, int pageSize){
		throw new UnsupportedOperationException();
	}

	@Override
	public Scanner<List<S3Object>> scanAfterPaged(BucketAndPrefix location, String startAfter, String delimiter){
		throw new UnsupportedOperationException();
	}

	@Override
	public Scanner<String> scanPrefixes(BucketAndPrefix locationPrefix, String startAfter, String delimiter){
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasCommonPrefixes(BucketAndPrefix locationPrefix, String delimiter){
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> getCommonPrefixes(BucketAndPrefix locationPrefix, String delimiter){
		throw new UnsupportedOperationException();
	}

	@Override
	public Scanner<DirectoryDto> scanSubdirectories(
			BucketAndPrefix locationPrefix,
			String startAfter,
			String delimiter,
			int pageSize,
			boolean currentDirectory){
		throw new UnsupportedOperationException();
	}

	@Override
	public Scanner<DirectoryDto> scanFilesOnly(
			BucketAndPrefix locationPrefix,
			String startAfter,
			String delimiter,
			int pageSize){
		throw new UnsupportedOperationException();
	}

	@Override
	public Scanner<DirectoryDto> scanSubdirectoriesOnly(
			BucketAndPrefix locationPrefix,
			String startAfter,
			String delimiter,
			int pageSize){
		throw new UnsupportedOperationException();
	}

	@Override
	public Region getCachedOrLatestRegionForBucket(String bucketName){
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<HeadObjectResponse> head(BucketAndKey bucketAndKey){
		throw new UnsupportedOperationException();
	}

	@Override
	public Scanner<List<BucketAndKeyVersionResult>> scanVersionsPaged(BucketAndPrefix location, int pageSize){
		throw new UnsupportedOperationException();
	}

	@Override
	public Scanner<List<BucketAndKeyVersionResult>> scanVersionsFromPaged(
			BucketAndPrefix location,
			String from,
			int pageSize){
		throw new UnsupportedOperationException();
	}

	@Override
	public Scanner<String> scanVersionPrefixes(BucketAndPrefix locationPrefix, String delimiter){
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasVersionCommonPrefixes(BucketAndPrefix locationPrefix, String delimiter){
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> getVersionCommonPrefixes(BucketAndPrefix locationPrefix, String delimiter){
		throw new UnsupportedOperationException();
	}

	/*---------- object read to local file -------*/

	@Override
	public void downloadToLocalFile(BucketAndKey location, Path localFilePath){
		throw new UnsupportedOperationException();
	}

	/*----------- object write from local file ----------*/

	@Override
	public void uploadLocalFileWithPublicRead(BucketAndKey location, ContentType contentType, Path localFilePath){
		throw new UnsupportedOperationException();
	}

	@Override
	public void uploadLocalFile(BucketAndKey location, ContentType contentType, Path localFilePath){
		Path destinationPath = testFolder.resolve(Path.of(location.bucket(), location.key()));
		try{
			Files.createDirectories(destinationPath.getParent());
			Files.copy(localFilePath, destinationPath);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

}
