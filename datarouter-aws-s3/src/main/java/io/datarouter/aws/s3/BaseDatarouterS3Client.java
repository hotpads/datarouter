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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.aws.s3.S3Headers.ContentType;
import io.datarouter.aws.s3.S3Headers.S3ContentType;
import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.ExposedByteArrayOutputStream;
import io.datarouter.bytes.InputStreamAndLength;
import io.datarouter.bytes.InputStreamTool;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.file.BucketAndKey;
import io.datarouter.storage.file.BucketAndPrefix;
import io.datarouter.storage.node.op.raw.read.DirectoryDto;
import io.datarouter.util.Require;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@SuppressWarnings("serial")
public abstract class BaseDatarouterS3Client implements DatarouterS3Client, Serializable{
	private static final Logger logger = LoggerFactory.getLogger(BaseDatarouterS3Client.class);

	// With S3 limit of 10_000 parts, this limits file size to 320 GiB
	private static final int MIN_UPLOAD_PART_SIZE_BYTES = ByteLength.ofMiB(32).toBytesInt();

	private final DatarouterS3ClientManager clientManager;

	/*------------ init ----------*/

	public BaseDatarouterS3Client(SerializableAwsCredentialsProviderProvider<?> awsCredentialsProviderProvider){
		this.clientManager = new DatarouterS3ClientManager(awsCredentialsProviderProvider);
	}

	/*--------- buckets ---------*/

	@Override
	public Scanner<Bucket> scanBuckets(){
		S3Client client = clientManager.getS3ClientForRegion(DatarouterS3ClientManager.DEFAULT_REGION);
		ListBucketsResponse response = client.listBuckets();
		return Scanner.of(response.buckets());
	}

	@Override
	public Region getRegionForBucket(String bucket){
		return clientManager.getBucketRegion(bucket);
	}

	@Override
	public Region getCachedOrLatestRegionForBucket(String bucket){
		return clientManager.getCachedOrLatestRegionForBucket(bucket);
	}

	/*--------- object head ---------*/

	@Override
	public Optional<HeadObjectResponse> head(BucketAndKey location){
		try{
			S3Client s3Client = clientManager.getS3ClientForBucket(location.bucket());
			HeadObjectRequest request = HeadObjectRequest.builder()
					.bucket(location.bucket())
					.key(location.key())
					.build();
			HeadObjectResponse response;
			try(var $ = TracerTool.startSpan("S3 headObject", TraceSpanGroupType.CLOUD_STORAGE)){
				response = s3Client.headObject(request);
				TracerTool.appendToSpanInfo("Content-Length", response.contentLength());
			}
			return Optional.of(response);
		}catch(NoSuchKeyException e){
			return Optional.empty();
		}
	}

	/*---------- object link ----------*/

	@Override
	public URL generateLink(BucketAndKey location, Duration expireAfter){
		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
				.bucket(location.bucket())
				.key(location.key())
				.build();
		GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
				.getObjectRequest(getObjectRequest)
				.signatureDuration(expireAfter)
				.build();
		return clientManager.getPresigner().presignGetObject(getObjectPresignRequest).url();
	}

	/*--------- object scan---------*/

	@Override
	public Scanner<List<S3Object>> scanPaged(BucketAndPrefix location){
		ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
				.bucket(location.bucket())
				.prefix(location.prefix())
				.build();
		ListObjectsV2Iterable listObjectsV2Iterable = clientManager.getS3ClientForBucket(location.bucket())
				.listObjectsV2Paginator(listObjectsV2Request);
		return Scanner.of(listObjectsV2Iterable)
				.map(ListObjectsV2Response::contents);
	}

	@Override
	public Scanner<S3Object> scanAfter(BucketAndPrefix location, String startAfter, String delimiter){
		var requestBuilder = ListObjectsV2Request.builder()
				.bucket(location.bucket());
		Optional.ofNullable(location.prefix()).ifPresent(requestBuilder::prefix);
		Optional.ofNullable(startAfter).ifPresent(requestBuilder::startAfter);
		Optional.ofNullable(delimiter).ifPresent(requestBuilder::delimiter);
		var responsePages = clientManager.getS3ClientForBucket(location.bucket())
				.listObjectsV2Paginator(requestBuilder.build());
		return Scanner.of(responsePages)
				.concatIter(ListObjectsV2Response::contents);
	}

	/*--------- object scan prefix---------*/

	@Override
	public Scanner<String> scanPrefixes(BucketAndPrefix locationPrefix, String startAfter, String delimiter){
		var requestBuilder = ListObjectsV2Request.builder()
				.bucket(locationPrefix.bucket());
		Optional.ofNullable(locationPrefix.prefix()).ifPresent(requestBuilder::prefix);
		Optional.ofNullable(startAfter).ifPresent(requestBuilder::startAfter);
		Optional.ofNullable(delimiter).ifPresent(requestBuilder::delimiter);
		var responsePages = clientManager.getS3ClientForBucket(locationPrefix.bucket())
				.listObjectsV2Paginator(requestBuilder.build());
		return Scanner.of(responsePages)
				.concatIter(ListObjectsV2Response::commonPrefixes)
				.map(CommonPrefix::prefix);
	}

	@Override
	public List<String> getCommonPrefixes(BucketAndPrefix locationPrefix, String delimiter){
		S3Client s3Client = clientManager.getS3ClientForBucket(locationPrefix.bucket());
		ListObjectsV2Request request = ListObjectsV2Request.builder()
				.bucket(locationPrefix.bucket())
				.prefix(locationPrefix.prefix())
				.delimiter(delimiter)
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
				.collect(Collectors.toList());
	}

	@Override
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

	@Override
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

	@Override
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

	/*---------- object delete ----------*/

	@Override
	public void delete(BucketAndKey location){
		DeleteObjectRequest request = DeleteObjectRequest.builder()
				.bucket(location.bucket())
				.key(location.key())
				.build();
		S3Client s3Client = clientManager.getS3ClientForBucket(location.bucket());
		try(var $ = TracerTool.startSpan("S3 deleteObject", TraceSpanGroupType.CLOUD_STORAGE)){
			s3Client.deleteObject(request);
		}
	}

	@Override
	public void deleteMulti(String bucket, Collection<String> keys){
		List<ObjectIdentifier> objectsToDelete = Scanner.of(keys)
				.map(key -> ObjectIdentifier.builder().key(key).build())
				.list();
		DeleteObjectsRequest request = DeleteObjectsRequest.builder()
				.bucket(bucket)
				.delete(Delete.builder().objects(objectsToDelete).build())
				.build();
		S3Client s3Client = clientManager.getS3ClientForBucket(bucket);
		try(var $ = TracerTool.startSpan("S3 deleteObjects", TraceSpanGroupType.CLOUD_STORAGE)){
			s3Client.deleteObjects(request);
			TracerTool.appendToSpanInfo("deleted", objectsToDelete.size());
		}
	}

	/*---------- data read bytes ---------*/

	@Override
	public ResponseInputStream<GetObjectResponse> getResponseInputStream(BucketAndKey location){
		S3Client s3Client = clientManager.getS3ClientForBucket(location.bucket());
		GetObjectRequest request = GetObjectRequest.builder()
				.bucket(location.bucket())
				.key(location.key())
				.build();
		ResponseInputStream<GetObjectResponse> response;
		try(var $ = TracerTool.startSpan("S3 getObject", TraceSpanGroupType.CLOUD_STORAGE)){
			response = s3Client.getObject(request);
			TracerTool.appendToSpanInfo("Content-Length", response.response().contentLength());
		}
		return response;
	}

	@Override
	public InputStream getInputStream(BucketAndKey location){
		return getResponseInputStream(location);
	}

	@Override
	public byte[] getObjectAsBytes(BucketAndKey location){
		S3Client s3Client = clientManager.getS3ClientForBucket(location.bucket());
		GetObjectRequest request = GetObjectRequest.builder()
				.bucket(location.bucket())
				.key(location.key())
				.build();
		ResponseBytes<GetObjectResponse> response;
		try(var $ = TracerTool.startSpan("S3 getObjectAsBytes", TraceSpanGroupType.CLOUD_STORAGE)){
			response = s3Client.getObjectAsBytes(request);
			TracerTool.appendToSpanInfo("Content-Length", response.response().contentLength());
		}
		return response.asByteArray();
	}

	@Override
	public byte[] getPartialObject(BucketAndKey location, long offset, int length){
		S3Client s3Client = clientManager.getS3ClientForBucket(location.bucket());
		String rangeParam = S3Tool.makeGetPartialObjectRangeParam(offset, length);
		GetObjectRequest request = GetObjectRequest.builder()
				.bucket(location.bucket())
				.key(location.key())
				.range(rangeParam)
				.build();
		ResponseBytes<GetObjectResponse> response;
		try(var $ = TracerTool.startSpan("S3 getPartialObject", TraceSpanGroupType.CLOUD_STORAGE)){
			response = s3Client.getObjectAsBytes(request);
			TracerTool.appendToSpanInfo("offset", offset);
			TracerTool.appendToSpanInfo("Content-Length", response.response().contentLength());
		}
		return response.asByteArray();
	}

	/*---------- data read files -------*/

	@Override
	public Path downloadFileToDirectory(BucketAndKey location, Path path){
		Path filePath = path.resolve(location.key());
		downloadFile(location, filePath);
		return filePath;
	}

	@Override
	public void downloadFile(BucketAndKey location, Path path){
		S3Tool.prepareLocalFileDestination(path);
		S3Client s3Client = clientManager.getS3ClientForBucket(location.bucket());
		GetObjectRequest request = GetObjectRequest.builder()
				.bucket(location.bucket())
				.key(location.key())
				.build();
		try(var $ = TracerTool.startSpan("S3 getObject", TraceSpanGroupType.CLOUD_STORAGE)){
			GetObjectResponse response = s3Client.getObject(request, ResponseTransformer.toFile(path));
			TracerTool.appendToSpanInfo("Content-Length", response.contentLength());
		}
	}

	/*---------- data write bytes ---------*/

	@Override
	public void copyObject(String bucket, String sourceKey, String destinationKey, ObjectCannedACL acl){
		CopyObjectRequest request = CopyObjectRequest.builder()
				.sourceBucket(bucket)
				.sourceKey(sourceKey)
				.destinationBucket(bucket)
				.destinationKey(destinationKey)
				.acl(acl)
				.build();
		S3Client s3Client = clientManager.getS3ClientForBucket(bucket);
		try(var $ = TracerTool.startSpan("S3 copyObject", TraceSpanGroupType.CLOUD_STORAGE)){
			s3Client.copyObject(request);
		}
	}

	@Override
	public void putObject(
			BucketAndKey location,
			ContentType contentType,
			byte[] bytes){
		S3Client s3Client = clientManager.getS3ClientForBucket(location.bucket());
		PutObjectRequest request = PutObjectRequest.builder()
				.bucket(location.bucket())
				.key(location.key())
				.contentType(contentType.getMimeType())
				.acl(ObjectCannedACL.PRIVATE)
				.build();
		RequestBody requestBody = RequestBody.fromBytes(bytes);
		try(var $ = TracerTool.startSpan("S3 putObject", TraceSpanGroupType.CLOUD_STORAGE)){
			s3Client.putObject(request, requestBody);
			TracerTool.appendToSpanInfo("Content-Length", request.contentLength());
		}
	}

	@Override
	public void putObjectWithPublicRead(
			BucketAndKey location,
			ContentType contentType,
			String cacheControl,
			ObjectCannedACL acl,
			byte[] bytes){
		S3Client s3Client = clientManager.getS3ClientForBucket(location.bucket());
		PutObjectRequest request = PutObjectRequest.builder()
				.bucket(location.bucket())
				.key(location.key())
				.contentType(contentType.getMimeType())
				.cacheControl(cacheControl)
				.acl(acl)
				.build();
		RequestBody requestBody = RequestBody.fromBytes(bytes);
		try(var $ = TracerTool.startSpan("S3 putObjectPublic", TraceSpanGroupType.CLOUD_STORAGE)){
			s3Client.putObject(request, requestBody);
			TracerTool.appendToSpanInfo("Content-Length", request.contentLength());
		}
	}

	@Override
	public void putObjectWithExpirationTime(
			BucketAndKey location,
			ContentType contentType,
			String cacheControl,
			ObjectCannedACL acl,
			byte[] bytes,
			Instant expirationTime){
		S3Client s3Client = clientManager.getS3ClientForBucket(location.bucket());
		PutObjectRequest request = PutObjectRequest.builder()
				.bucket(location.bucket())
				.key(location.key())
				.contentType(contentType.getMimeType())
				.cacheControl(cacheControl)
				.acl(acl)
				.expires(expirationTime)
				.build();
		RequestBody requestBody = RequestBody.fromBytes(bytes);
		try(var $ = TracerTool.startSpan("S3 putObject", TraceSpanGroupType.CLOUD_STORAGE)){
			s3Client.putObject(request, requestBody);
			TracerTool.appendToSpanInfo("Content-Length", request.contentLength());
		}
	}

	/*---------- data write bytes multipart OutputStream ---------*/

	@Override
	public OutputStream put(BucketAndKey location, S3ContentType contentType){
		return multipartUpload(location, contentType, Optional.empty());
	}

	@Override
	public OutputStream putWithPublicRead(BucketAndKey location, S3ContentType contentType){
		return multipartUpload(location, contentType, Optional.of(ObjectCannedACL.PUBLIC_READ));
	}

	private OutputStream multipartUpload(
			BucketAndKey location,
			S3ContentType contentType,
			Optional<ObjectCannedACL> aclOpt){
		S3Client s3Client = clientManager.getS3ClientForBucket(location.bucket());
		String uploadId = createMultipartUploadRequest(location, contentType, aclOpt);
		var buffer = new ExposedByteArrayOutputStream(ByteLength.ofKiB(64).toBytesInt());
		List<CompletedPart> completedParts = new ArrayList<>();
		return new OutputStream(){

			private boolean closed;

			private void uploadPart(){
				UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
						.bucket(location.bucket())
						.key(location.key())
						.uploadId(uploadId)
						.partNumber(completedParts.size() + 1)
						.build();
				// ExposedByteArrayOutputStream.toInputStream() avoids duplicating the data.
				InputStream bufferInputStream = buffer.toInputStream();
				// RequestBody.fromInputStream avoids duplicating the data.
				RequestBody requestBody = RequestBody.fromInputStream(bufferInputStream, buffer.size());
				UploadPartResponse response;
				try(var $ = TracerTool.startSpan("S3 uploadPart", TraceSpanGroupType.CLOUD_STORAGE)){
					response = s3Client.uploadPart(uploadPartRequest, requestBody);
					TracerTool.appendToSpanInfo("Content-Length", uploadPartRequest.contentLength());
				}
				String eTag = response.eTag();
				CompletedPart completedPart = CompletedPart.builder()
						.partNumber(completedParts.size() + 1)
						.eTag(eTag)
						.build();
				completedParts.add(completedPart);
				// don't reset the buffer until the upload has completed
				buffer.reset();
			}

			private void checkBufferSize(){
				if(buffer.size() > MIN_UPLOAD_PART_SIZE_BYTES){
					uploadPart();
				}
			}

			@Override
			public void write(int by){
				Require.isFalse(closed);
				buffer.write(by);
				checkBufferSize();
			}

			@Override
			public void write(byte[] by, int off, int len){
				Require.isFalse(closed);
				buffer.write(by, off, len);
				checkBufferSize();
			}

			@Override
			public void close(){
				uploadPart();
				completeMultipartUploadRequest(location, uploadId, completedParts);
				closed = true;
			}

		};
	}

	/*---------- data write bytes multipart InputStream ---------*/

	/*
	 * This has a slight advantage over multithreadUpload in that it reuses the buffer to reduce memory turnover.
	 * They should probably be combined somehow though.
	 */
	@Override
	public void multipartUpload(BucketAndKey location, S3ContentType contentType, InputStream inputStream){
		S3Client s3Client = clientManager.getS3ClientForBucket(location.bucket());
		String uploadId = createMultipartUploadRequest(location, contentType, Optional.empty());
		int partNumber = 1;//s3 part ids start at 1
		byte[] buffer = new byte[MultipartUploadPartSize.sizeForPart(partNumber)];
		List<CompletedPart> completedParts = new ArrayList<>();
		int numBytesRead;
		try(var $inputStream = inputStream){
			while((numBytesRead = InputStreamTool.readUntilLength(inputStream, buffer, 0, buffer.length)) > 0){
				UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
						.bucket(location.bucket())
						.key(location.key())
						.uploadId(uploadId)
						.partNumber(partNumber)
						.build();
				InputStream bufferAsInputStream = new ByteArrayInputStream(buffer, 0, numBytesRead);
				RequestBody requestBody = RequestBody.fromInputStream(bufferAsInputStream, numBytesRead);
				UploadPartResponse uploadPartResponse;
				try(var $ = TracerTool.startSpan("S3 uploadPart", TraceSpanGroupType.CLOUD_STORAGE)){
					uploadPartResponse = s3Client.uploadPart(uploadPartRequest, requestBody);
					TracerTool.appendToSpanInfo("Content-Length", uploadPartRequest.contentLength());
					logger.info("Uploaded {}, partId={}, size={}",
							location,
							partNumber,
							ByteLength.ofBytes(numBytesRead).toDisplay());
				}
				CompletedPart completedPart = CompletedPart.builder()
						.partNumber(partNumber)
						.eTag(uploadPartResponse.eTag())
						.build();
				completedParts.add(completedPart);
				++partNumber;
				int nextBufferSize = MultipartUploadPartSize.sizeForPart(partNumber);
				if(buffer.length != nextBufferSize){// try to reuse buffer
					buffer = new byte[nextBufferSize];
				}
			}
			completeMultipartUploadRequest(location, uploadId, completedParts);
		}catch(IOException | RuntimeException e){
			abortMultipartUploadRequest(location, uploadId);
			String message = String.format("Error on %s", location);
			throw new RuntimeException(message, e);
		}
	}

	private record MultithreadUploadPartNumberAndBytes(
			int partNumber,
			byte[] bytes,
			int numFilledBytes){

		boolean hasAnyData(){
			return numFilledBytes > 0;
		}

		ByteArrayInputStream toInputStream(){
			return new ByteArrayInputStream(bytes, 0, numFilledBytes);
		}
	}

	@Override
	public void multithreadUpload(
			BucketAndKey location,
			S3ContentType contentType,
			InputStream inputStream,
			Threads threads,
			ByteLength minPartSize){
		String uploadId = createMultipartUploadRequest(location, contentType, Optional.empty());
		try{
			Scanner.iterate(1, partNumber -> partNumber + 1)// S3 part ids start at 1
					.map(partNumber -> {
						int sizeForPart = Math.max(
								MultipartUploadPartSize.sizeForPart(partNumber),
								minPartSize.toBytesInt());
						byte[] buffer = new byte[sizeForPart];
						int result = InputStreamTool.readUntilLength(
								inputStream,
								buffer,
								0,
								buffer.length);
						int numRead = Math.max(result, 0);//remove potential -1
						return new MultithreadUploadPartNumberAndBytes(partNumber, buffer, numRead);
					})
					.advanceWhile(MultithreadUploadPartNumberAndBytes::hasAnyData)
					.parallelUnordered(threads)
					.map(partNumberAndBytes -> {
						int partNumber = partNumberAndBytes.partNumber();
						int length = partNumberAndBytes.numFilledBytes();
						var data = new InputStreamAndLength(partNumberAndBytes.toInputStream(), length);
						return uploadPart(location, uploadId, partNumber, data);
					})
					.flush(completedParts -> completeMultipartUploadRequest(location, uploadId, completedParts));
		}catch(RuntimeException e){
			abortMultipartUploadRequest(location, uploadId);
			String message = String.format("Error on %s", location);
			throw new RuntimeException(message, e);
		}finally{
			InputStreamTool.close(inputStream);
		}
	}

	/*---------- data write bytes multipart Scanner ---------*/

	private record MultithreadUploadPartNumberAndData(
			int number,
			InputStreamAndLength data){
	}

	@Override
	public void multithreadUpload(
			BucketAndKey location,
			S3ContentType contentType,
			Scanner<InputStreamAndLength> parts,
			Threads threads){
		String uploadId = createMultipartUploadRequest(location, contentType, Optional.empty());
		var partNumberTracker = new AtomicInteger(1);//s3 partNumbers start at 1
		try{
			parts
					.map(inputStreamAndLength -> new MultithreadUploadPartNumberAndData(
							partNumberTracker.getAndIncrement(),
							inputStreamAndLength))
					.parallelUnordered(threads)
					.map(part -> uploadPart(location, uploadId, part.number(), part.data()))
					.flush(completedParts -> completeMultipartUploadRequest(location, uploadId, completedParts));
		}catch(RuntimeException e){
			abortMultipartUploadRequest(location, uploadId);
			String message = String.format("Error on %s", location);
			throw new RuntimeException(message, e);
		}
	}

	/*----------- data write bytes multipart sub-ops ----------*/

	@Override
	public String createMultipartUploadRequest(
			BucketAndKey location,
			S3ContentType contentType,
			Optional<ObjectCannedACL> aclOpt){
		CreateMultipartUploadRequest createRequest = CreateMultipartUploadRequest.builder()
				.acl(aclOpt.orElse(null))
				.bucket(location.bucket())
				.key(location.key())
				.contentType(contentType.getMimeType())
				.build();
		CreateMultipartUploadResponse createResponse;
		S3Client s3Client = clientManager.getS3ClientForBucket(location.bucket());
		try(var $ = TracerTool.startSpan("S3 createMultipartUpload", TraceSpanGroupType.CLOUD_STORAGE)){
			createResponse = s3Client.createMultipartUpload(createRequest);
		}
		return createResponse.uploadId();
	}

	@Override
	public CompletedPart uploadPart(
			BucketAndKey location,
			String uploadId,
			int partNumber,
			InputStreamAndLength inputStreamAndLength){
		UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
				.bucket(location.bucket())
				.key(location.key())
				.uploadId(uploadId)
				.partNumber(partNumber)
				.build();
		RequestBody requestBody = RequestBody.fromInputStream(
				inputStreamAndLength.inputStream(),
				inputStreamAndLength.length());
		S3Client s3Client = clientManager.getS3ClientForBucket(location.bucket());
		UploadPartResponse uploadPartResponse;
		try(var $ = TracerTool.startSpan("S3 parallelUploadPart", TraceSpanGroupType.CLOUD_STORAGE)){
			uploadPartResponse = s3Client.uploadPart(uploadPartRequest, requestBody);
			TracerTool.appendToSpanInfo("Content-Length", uploadPartRequest.contentLength());
			logger.info(
					"Uploaded {}, partNumber={}, size={}",
					location,
					partNumber,
					ByteLength.ofBytes(inputStreamAndLength.length()).toDisplay());
		}
		return CompletedPart.builder()
				.partNumber(partNumber)
				.eTag(uploadPartResponse.eTag())
				.build();
	}

	@Override
	public void completeMultipartUploadRequest(
			BucketAndKey location,
			String uploadId,
			List<CompletedPart> completedParts){
		CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
				// CompleteMultipartUploadRequest requires the parts be sorted
				.parts(Scanner.of(completedParts)
						.sort(Comparator.comparing(CompletedPart::partNumber))
						.list())
				.build();
		CompleteMultipartUploadRequest completeMultipartUploadRequest = CompleteMultipartUploadRequest.builder()
				.bucket(location.bucket())
				.key(location.key())
				.uploadId(uploadId)
				.multipartUpload(completedMultipartUpload)
				.build();
		S3Client s3Client = clientManager.getS3ClientForBucket(location.bucket());
		try(var $ = TracerTool.startSpan("S3 completeMultipartUpload", TraceSpanGroupType.CLOUD_STORAGE)){
			s3Client.completeMultipartUpload(completeMultipartUploadRequest);
		}
	}

	@Override
	public void abortMultipartUploadRequest(
			BucketAndKey location,
			String uploadId){
		logger.warn("Aborting {}", location);
		AbortMultipartUploadRequest abortRequest = AbortMultipartUploadRequest.builder()
				.bucket(location.bucket())
				.key(location.key())
				.uploadId(uploadId)
				.build();
		S3Client s3Client = clientManager.getS3ClientForBucket(location.bucket());
		try(var $ = TracerTool.startSpan("S3 abortMultipartUpload", TraceSpanGroupType.CLOUD_STORAGE)){
			s3Client.abortMultipartUpload(abortRequest);
		}
		logger.warn("Aborted {}", location);
	}

	/*----------- data write files ----------*/

	@Override
	public void putFile(BucketAndKey location, ContentType contentType, Path path){
		S3Client s3Client = clientManager.getS3ClientForBucket(location.bucket());
		PutObjectRequest request = PutObjectRequest.builder()
				.bucket(location.bucket())
				.key(location.key())
				.contentType(contentType.getMimeType())
				.acl(ObjectCannedACL.PRIVATE)
				.build();
		RequestBody requestBody = RequestBody.fromFile(path);
		try(var $ = TracerTool.startSpan("S3 putFile", TraceSpanGroupType.CLOUD_STORAGE)){
			s3Client.putObject(request, requestBody);
			TracerTool.appendToSpanInfo("Content-Length", request.contentLength());
		}
	}

	@Override
	public void putFilePublic(BucketAndKey location, ContentType contentType, Path path){
		S3Client s3Client = clientManager.getS3ClientForBucket(location.bucket());
		PutObjectRequest request = PutObjectRequest.builder()
				.bucket(location.bucket())
				.key(location.key())
				.contentType(contentType.getMimeType())
				.acl(ObjectCannedACL.PUBLIC_READ)
				.build();
		RequestBody requestBody = RequestBody.fromFile(path);
		try(var $ = TracerTool.startSpan("S3 putFilePublic", TraceSpanGroupType.CLOUD_STORAGE)){
			s3Client.putObject(request, requestBody);
			TracerTool.appendToSpanInfo("Content-Length", request.contentLength());
		}
	}

}
