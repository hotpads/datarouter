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
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import io.datarouter.aws.s3.S3Headers.ContentType;
import io.datarouter.aws.s3.S3Headers.S3ContentType;
import io.datarouter.aws.s3.request.DatarouterS3BucketRequests;
import io.datarouter.aws.s3.request.DatarouterS3FileRequests;
import io.datarouter.aws.s3.request.DatarouterS3MultipartRequests;
import io.datarouter.aws.s3.request.DatarouterS3ObjectRequests;
import io.datarouter.aws.s3.request.DatarouterS3PublicRequests;
import io.datarouter.aws.s3.request.DatarouterS3VersionRequests;
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
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.S3Object;

@SuppressWarnings("serial")
public abstract class BaseDatarouterS3Client implements DatarouterS3Client, Serializable{

	private final DatarouterS3ClientManager clientManager;
	private final DatarouterS3FileRequests fileRequests;
	private final DatarouterS3BucketRequests bucketRequests;
	private final DatarouterS3MultipartRequests multipartRequests;
	private final DatarouterS3ObjectRequests objectRequests;
	private final DatarouterS3PublicRequests publicRequests;
	private final DatarouterS3VersionRequests versionRequests;

	public BaseDatarouterS3Client(SerializableAwsCredentialsProviderProvider<?> awsCredentialsProviderProvider){
		clientManager = new DatarouterS3ClientManager(awsCredentialsProviderProvider);
		fileRequests = new DatarouterS3FileRequests(clientManager);
		bucketRequests = new DatarouterS3BucketRequests(clientManager);
		multipartRequests = new DatarouterS3MultipartRequests(clientManager);
		objectRequests = new DatarouterS3ObjectRequests(clientManager);
		publicRequests = new DatarouterS3PublicRequests(clientManager);
		versionRequests = new DatarouterS3VersionRequests(clientManager);
	}

	/*--------- regions ---------*/

	@Override
	public Region getRegionForBucket(String bucket){
		return clientManager.getBucketRegion(bucket);
	}

	@Override
	public Region getCachedOrLatestRegionForBucket(String bucket){
		return clientManager.getCachedOrLatestRegionForBucket(bucket);
	}

	/*--------- metadata buckets ---------*/

	@Override
	public Scanner<Bucket> scanBuckets(){
		return bucketRequests.scanBuckets();
	}

	/*--------- metadata head ---------*/

	@Override
	public Optional<HeadObjectResponse> head(BucketAndKey location){
		return objectRequests.head(location);
	}

	/*---------- metadata generate link ----------*/

	@Override
	public URL generateLink(BucketAndKey location, Duration expireAfter){
		return publicRequests.generateLink(location, expireAfter);
	}

	/*--------- metadata scan---------*/

	@Override
	public Scanner<List<S3Object>> scanPaged(BucketAndPrefix location, int pageSize){
		return bucketRequests.scanPaged(location, pageSize);
	}

	@Override
	public Scanner<List<S3Object>> scanAfterPaged(BucketAndPrefix location, String startAfter, String delimiter){
		return bucketRequests.scanAfterPaged(location, startAfter, delimiter);
	}

	/*--------- metadata scan prefix---------*/

	@Override
	public Scanner<String> scanPrefixes(BucketAndPrefix locationPrefix, String startAfter, String delimiter){
		return bucketRequests.scanPrefixes(locationPrefix, startAfter, delimiter);
	}

	@Override
	public boolean hasCommonPrefixes(BucketAndPrefix locationPrefix, String delimiter){
		return bucketRequests.hasCommonPrefixes(locationPrefix, delimiter);
	}

	@Override
	public List<String> getCommonPrefixes(BucketAndPrefix locationPrefix, String delimiter){
		return bucketRequests.getCommonPrefixes(locationPrefix, delimiter);
	}

	@Override
	public Scanner<DirectoryDto> scanSubdirectories(
			BucketAndPrefix locationPrefix,
			String startAfter,
			String delimiter,
			int pageSize,
			boolean currentDirectory){
		return bucketRequests.scanSubdirectories(locationPrefix, startAfter, delimiter, pageSize, currentDirectory);
	}

	@Override
	public Scanner<DirectoryDto> scanSubdirectoriesOnly(
			BucketAndPrefix locationPrefix,
			String startAfter,
			String delimiter,
			int pageSize){
		return bucketRequests.scanSubdirectoriesOnly(locationPrefix, startAfter, delimiter, pageSize);
	}

	@Override
	public Scanner<DirectoryDto> scanFilesOnly(
			BucketAndPrefix locationPrefix,
			String startAfter,
			String delimiter,
			int pageSize){
		return bucketRequests.scanFilesOnly(locationPrefix, startAfter, delimiter, pageSize);
	}

	/*---------- scan object versions ---------*/

	@Override
	public Scanner<List<BucketAndKeyVersionResult>> scanVersionsPaged(
			BucketAndPrefix location,
			int pageSize){
		return versionRequests.scanVersionsPaged(location, pageSize);
	}

	@Override
	public Scanner<List<BucketAndKeyVersionResult>> scanVersionsFromPaged(
			BucketAndPrefix location,
			String from,
			int pageSize){
		return versionRequests.scanVersionsFromPaged(location, from, pageSize);
	}

	@Override
	public Scanner<String> scanVersionPrefixes(BucketAndPrefix locationPrefix, String delimiter){
		return versionRequests.scanVersionPrefixes(locationPrefix, delimiter);
	}

	@Override
	public boolean hasVersionCommonPrefixes(BucketAndPrefix locationPrefix, String delimiter){
		return versionRequests.hasVersionCommonPrefixes(locationPrefix, delimiter);
	}

	@Override
	public List<String> getVersionCommonPrefixes(BucketAndPrefix locationPrefix, String delimiter){
		return versionRequests.getVersionCommonPrefixes(locationPrefix, delimiter);
	}

	/*---------- object delete ----------*/

	@Override
	public void delete(BucketAndKey location){
		objectRequests.delete(location);
	}

	@Override
	public void deleteMulti(BucketAndKeys bucketAndKeys){
		objectRequests.deleteMulti(bucketAndKeys);
	}

	@Override
	public void deleteVersion(BucketAndKeyVersion bucketAndKeyVersion){
		versionRequests.deleteVersion(bucketAndKeyVersion);
	}

	@Override
	public void deleteVersions(BucketAndKeyVersions bucketAndKeyVersions){
		versionRequests.deleteVersions(bucketAndKeyVersions);
	}

	/*---------- object read bytes ---------*/

	@Override
	public ResponseInputStream<GetObjectResponse> getResponseInputStream(BucketAndKey location){
		return objectRequests.getResponseInputStream(location);
	}

	@Override
	public InputStream getInputStream(BucketAndKey location){
		return objectRequests.getResponseInputStream(location);
	}

	@Override
	public Optional<byte[]> findObject(BucketAndKey location){
		return objectRequests.findObject(location);
	}

	@Override
	public Optional<byte[]> findPartialObject(BucketAndKey location, long offset, int length){
		return objectRequests.findPartialObject(location, offset, length);
	}

	@Override
	public Optional<byte[]> findEnding(BucketAndKey location, int length){
		return objectRequests.findEnding(location, length);
	}

	/*---------- object write ---------*/

	@Override
	public void copyObject(String sourceBucket, String sourceKey, String destinationBucket,
			String destinationKey, ObjectCannedACL acl){
		objectRequests.copyObject(sourceBucket, sourceKey, destinationBucket, destinationKey, acl);
	}

	@Override
	public void putObject(
			BucketAndKey location,
			S3ContentType contentType,
			byte[] bytes){
		objectRequests.putObject(location, contentType, bytes);
	}

	@Override
	public void putObjectWithPublicRead(
			BucketAndKey location,
			ContentType contentType,
			String cacheControl,
			ObjectCannedACL acl,
			byte[] bytes){
		publicRequests.putObjectWithPublicRead(location, contentType, cacheControl, acl, bytes);
	}

	@Override
	public void putObjectWithPublicReadAndExpirationTime(
			BucketAndKey location,
			ContentType contentType,
			String cacheControl,
			byte[] bytes,
			Instant expirationTime){
		publicRequests.putObjectWithPublicReadAndExpirationTime(
				location,
				contentType,
				cacheControl,
				bytes,
				expirationTime);
	}

	/*---------- object write multipart OutputStream ---------*/

	/**
	 * @deprecated  Use the InputStream based methods
	 */
	@Deprecated
	@Override
	public OutputStream multipartUploadOutputStream(
			BucketAndKey location,
			S3ContentType contentType){
		return multipartRequests.multipartUploadOutputStream(location, contentType);
	}

	/*---------- object write multipart InputStream ---------*/

	@Override
	public void multipartUpload(
			BucketAndKey location,
			S3ContentType contentType,
			InputStream inputStream){
		multipartRequests.multipartUpload(location, contentType, inputStream);
	}

	@Override
	public void multipartUploadWithPublicRead(
			BucketAndKey location,
			S3ContentType contentType,
			InputStream inputStream){
		publicRequests.multipartUploadWithPublicRead(location, contentType, inputStream);
	}

	@Override
	public void multithreadUpload(
			BucketAndKey location,
			S3ContentType contentType,
			InputStream inputStream,
			Threads threads,
			ByteLength minPartSize){
		multipartRequests.multithreadUpload(location, contentType, inputStream, threads, minPartSize);
	}

	/*---------- object write multipart Scanner ---------*/

	@Override
	public void multithreadUpload(
			BucketAndKey location,
			S3ContentType contentType,
			Scanner<InputStreamAndLength> parts,
			Threads threads){
		multipartRequests.multithreadUpload(location, contentType, parts, threads);
	}

	/*----------- object write multipart sub-ops ----------*/

	@Override
	public String createMultipartUploadRequest(
			BucketAndKey location,
			S3ContentType contentType,
			Optional<ObjectCannedACL> aclOpt){
		return multipartRequests.createMultipartUploadRequest(location, contentType, aclOpt);
	}

	@Override
	public CompletedPart uploadPart(
			BucketAndKey location,
			String uploadId,
			int partNumber,
			InputStreamAndLength inputStreamAndLength){
		return multipartRequests.uploadPart(location, uploadId, partNumber, inputStreamAndLength);
	}

	@Override
	public void completeMultipartUploadRequest(
			BucketAndKey location,
			String uploadId,
			List<CompletedPart> completedParts){
		multipartRequests.completeMultipartUploadRequest(location, uploadId, completedParts);
	}

	@Override
	public void abortMultipartUploadRequest(
			BucketAndKey location,
			String uploadId){
		multipartRequests.abortMultipartUploadRequest(location, uploadId);
	}

	/*---------- object read to local file -------*/

	@Override
	public void downloadToLocalFile(BucketAndKey location, Path localFilePath){
		fileRequests.downloadToLocalFile(location, localFilePath);
	}

	/*----------- object write from local file ----------*/

	@Override
	public void uploadLocalFile(BucketAndKey location, ContentType contentType, Path localFilePath){
		fileRequests.uploadLocalFile(location, contentType, localFilePath);
	}

	@Override
	public void uploadLocalFileWithPublicRead(BucketAndKey location, ContentType contentType, Path localFilePath){
		publicRequests.uploadLocalFileWithPublicRead(location, contentType, localFilePath);
	}

}
