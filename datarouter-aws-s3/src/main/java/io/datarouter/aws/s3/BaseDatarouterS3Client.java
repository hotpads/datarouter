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
package io.datarouter.aws.s3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.Transfer;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.TransferProgress;
import com.amazonaws.services.s3.transfer.Upload;

import io.datarouter.aws.s3.S3Headers.ContentType;
import io.datarouter.aws.s3.S3Headers.S3ContentType;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.node.op.raw.read.DirectoryDto;
import io.datarouter.util.Require;
import io.datarouter.util.bytes.ByteUnitTool;
import io.datarouter.util.concurrent.ThreadTool;
import io.datarouter.util.io.ReaderTool;
import io.datarouter.util.number.NumberFormatter;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetBucketLocationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketLocationResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@SuppressWarnings("serial")
public abstract class BaseDatarouterS3Client implements DatarouterS3Client, Serializable{
	private static final Logger logger = LoggerFactory.getLogger(BaseDatarouterS3Client.class);

	/**
	 * With AWS SDK v2, you need to know the region of the bucket when you make a query to it. This client manages
	 * multiple underlying clients for each region. It will try its best to determine the region of a bucket. To do
	 * this, it uses the getBucketLocation API. This API returns the region of the bucket, if the credentials have
	 * permissions to call it. Otherwise, if called on US-EAST-1, it throws an exception that reveals the region of the
	 * bucket. If called on another region, it throws a 403. Therefore, by parsing the exception, we can determine the
	 * region of all buckets at runtime. We're probably going to persist this information in the future in case their
	 * API changes.
	 */
	private static final Pattern EXPECTED_REGION_EXTRACTOR = Pattern.compile("expecting '(.*)'");
	private static final int MIN_UPLOAD_PART_SIZE_BYTES = 5 * 1024 * 1024;
	private static final Region DEFAULT_REGION = Region.US_EAST_1;

	private final SerializableAwsCredentialsProviderProvider<?> awsCredentialsProviderProvider;

	private transient Map<Region,S3Client> s3ClientByRegion;
	private transient Map<String,Region> regionByBucket;
	private transient S3Presigner s3Presigner;
	private transient Map<Region,TransferManager> transferManagerByRegion;

	public BaseDatarouterS3Client(SerializableAwsCredentialsProviderProvider<?> awsCredentialsProviderProvider){
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

	@Override
	public Scanner<Bucket> scanBuckets(){
		S3Client client = getS3ClientForRegion(DEFAULT_REGION);
		ListBucketsResponse response = client.listBuckets();
		return Scanner.of(response.buckets());
	}

	@Override
	public void copyObject(String bucket, String sourceKey, String destinationKey, ObjectCannedACL acl){
		String source = URLEncoder.encode(bucket + "/" + sourceKey, StandardCharsets.UTF_8);
		CopyObjectRequest request = CopyObjectRequest.builder()
				.copySource(source)
				// .sourceKey(sourceKey)
				// .sourceBucket(bucket)
				// .sourceVersionId(?)
				.destinationBucket(bucket)
				.destinationKey(destinationKey)
				.acl(acl)
				.build();
		S3Client s3Client = getS3ClientForBucket(bucket);
		try(var $ = TracerTool.startSpan("S3 copyObject", TraceSpanGroupType.CLOUD_STORAGE)){
			s3Client.copyObject(request);
		}
	}

	@Override
	public void deleteObject(String bucket, String key){
		DeleteObjectRequest request = DeleteObjectRequest.builder()
				.bucket(bucket)
				.key(key)
				.build();
		S3Client s3Client = getS3ClientForBucket(bucket);
		try(var $ = TracerTool.startSpan("S3 deleteObject", TraceSpanGroupType.CLOUD_STORAGE)){
			s3Client.deleteObject(request);
		}
	}

	@Override
	public void putObjectWithHeartbeat(String bucket, String key, ContentType contentType, Path path,
			Runnable heartbeat){
		com.amazonaws.services.s3.model.PutObjectRequest putObjectRequest
				= new com.amazonaws.services.s3.model.PutObjectRequest(bucket, key, path.toFile());
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType(contentType.getMimeType());
		putObjectRequest.setMetadata(metadata);
		Upload upload = getTransferManagerForBucket(bucket).upload(putObjectRequest);
		handleTransfer(upload, heartbeat);
	}

	@Override
	public BufferedWriter putAsWriter(String bucket, String key, ContentType contentType){
		return new BufferedWriter(new OutputStreamWriter(put(bucket, key, contentType)));
	}

	@Override
	public OutputStream put(String bucket, String key, S3ContentType contentType){
		S3Client s3Client = getS3ClientForBucket(bucket);
		CreateMultipartUploadRequest request = CreateMultipartUploadRequest.builder()
				.bucket(bucket)
				.key(key)
				.contentType(contentType.getMimeType())
				.build();
		CreateMultipartUploadResponse response;
		try(var $ = TracerTool.startSpan("S3 createMultipartUpload", TraceSpanGroupType.CLOUD_STORAGE)){
			response = s3Client.createMultipartUpload(request);
		}
		String uploadId = response.uploadId();
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		List<CompletedPart> completedParts = new ArrayList<>();
		return new OutputStream(){

			private boolean closed;

			private void uploadPart(){
				UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
						.bucket(bucket)
						.key(key)
						.uploadId(uploadId)
						.partNumber(completedParts.size() + 1)
						.build();
				RequestBody requestBody = RequestBody.fromBytes(buffer.toByteArray());
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
				CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
						.parts(completedParts)
						.build();
				CompleteMultipartUploadRequest completeMultipartUploadRequest = CompleteMultipartUploadRequest.builder()
						.bucket(bucket)
						.key(key)
						.uploadId(uploadId)
						.multipartUpload(completedMultipartUpload)
						.build();
				try(var $ = TracerTool.startSpan("S3 completeMultipartUpload", TraceSpanGroupType.CLOUD_STORAGE)){
					s3Client.completeMultipartUpload(completeMultipartUploadRequest);
				}
				closed = true;
			}

		};
	}

	@Override
	public void putObjectAsString(String bucket, String key, ContentType contentType, String content){
		S3Client s3Client = getS3ClientForBucket(bucket);
		PutObjectRequest request = makePutObjectRequest(bucket, key, contentType);
		RequestBody requestBody = RequestBody.fromString(content);
		try(var $ = TracerTool.startSpan("S3 putObject", TraceSpanGroupType.CLOUD_STORAGE)){
			s3Client.putObject(request, requestBody);
			TracerTool.appendToSpanInfo("Content-Length", request.contentLength());
		}
	}

	@Override
	public void putObjectAsBytes(String bucket, String key, ContentType contentType, String cacheControl,
			ObjectCannedACL acl, byte[] bytes){
		S3Client s3Client = getS3ClientForBucket(bucket);
		PutObjectRequest request = makePutObjectRequestBuilder(bucket, key, contentType)
				.cacheControl(cacheControl)
				.acl(acl)
				.build();
		RequestBody requestBody = RequestBody.fromBytes(bytes);
		try(var $ = TracerTool.startSpan("S3 putObject", TraceSpanGroupType.CLOUD_STORAGE)){
			s3Client.putObject(request, requestBody);
			TracerTool.appendToSpanInfo("Content-Length", request.contentLength());
		}
	}

	@Override
	public void putPublicObject(String bucket, String key, ContentType contentType, Path path){
		putObjectWithAcl(bucket, key, contentType, path, ObjectCannedACL.PUBLIC_READ);
	}

	@Override
	public void putObject(String bucket, String key, ContentType contentType, Path path){
		putObjectWithAcl(bucket, key, contentType, path, ObjectCannedACL.PRIVATE);
	}

	@Override
	public void downloadFilesToDirectory(String bucket, String prefix, Path path){
		scanObjects(bucket, prefix)
				.map(S3Object::key)
				.forEach(key -> downloadFileToDirectory(bucket, key, path));
	}

	@Override
	public Path downloadFileToDirectory(String bucket, String key, Path path){
		Path filePath = path.resolve(key);
		downloadFile(bucket, key, filePath);
		return filePath;
	}

	@Override
	public void downloadFile(String bucket, String key, Path path){
		prepareFileDestination(path);
		S3Client s3Client = getS3ClientForBucket(bucket);
		GetObjectRequest request = makeGetObjectRequest(bucket, key);
		try(var $ = TracerTool.startSpan("S3 getObject", TraceSpanGroupType.CLOUD_STORAGE)){
			GetObjectResponse response = s3Client.getObject(request, ResponseTransformer.toFile(path));
			TracerTool.appendToSpanInfo("Content-Length", response.contentLength());
		}
	}

	@Override
	public void downloadFileWithHeartbeat(String bucket, String key, Path path, Runnable heartbeat){
		prepareFileDestination(path);
		TransferManager transferManager = getTransferManagerForBucket(bucket);
		Download download;
		try(var $ = TracerTool.startSpan("S3 download", TraceSpanGroupType.CLOUD_STORAGE)){
			download = transferManager.download(bucket, key, path.toFile());
		}
		handleTransfer(download, heartbeat);
	}

	@Override
	public Scanner<List<String>> scanBatchesOfLinesWithPrefix(String bucket, String prefix, int batchSize){
		return scanObjects(bucket, prefix)
				.map(S3Object::key)
				.concat(key -> scanBatchesOfLines(bucket, key, batchSize));
	}

	@Override
	public Scanner<String> scanLines(String bucket, String key){
		return ReaderTool.lines(new BufferedReader(new InputStreamReader(getObject(bucket, key))));
	}

	@Override
	public Scanner<List<String>> scanBatchesOfLines(String bucket, String key, int batchSize){
		return scanLines(bucket, key)
				.batch(batchSize);
	}

	@Override
	public ResponseInputStream<GetObjectResponse> getObjectResponse(String bucket, String key){
		S3Client s3Client = getS3ClientForBucket(bucket);
		GetObjectRequest request = makeGetObjectRequest(bucket, key);
		ResponseInputStream<GetObjectResponse> response;
		try(var $ = TracerTool.startSpan("S3 getObject", TraceSpanGroupType.CLOUD_STORAGE)){
			response = s3Client.getObject(request);
			TracerTool.appendToSpanInfo("Content-Length", response.response().contentLength());
		}
		return response;
	}

	@Override
	public InputStream getObject(String bucket, String key){
		return getObjectResponse(bucket, key);
	}

	@Override
	public byte[] getObjectAsBytes(String bucket, String key){
		S3Client s3Client = getS3ClientForBucket(bucket);
		GetObjectRequest request = makeGetObjectRequest(bucket, key);
		ResponseBytes<GetObjectResponse> response;
		try(var $ = TracerTool.startSpan("S3 getObjectAsBytes", TraceSpanGroupType.CLOUD_STORAGE)){
			response = s3Client.getObjectAsBytes(request);
			TracerTool.appendToSpanInfo("Content-Length", response.response().contentLength());
		}
		return response.asByteArray();
	}

	@Override
	public byte[] getPartialObject(String bucket, String key, long offset, int length){
		S3Client s3Client = getS3ClientForBucket(bucket);
		GetObjectRequest request = makeGetPartialObjectRequest(bucket, key, offset, length);
		ResponseBytes<GetObjectResponse> response;
		try(var $ = TracerTool.startSpan("S3 getPartialObject", TraceSpanGroupType.CLOUD_STORAGE)){
			response = s3Client.getObjectAsBytes(request);
			TracerTool.appendToSpanInfo("offset", offset);
			TracerTool.appendToSpanInfo("Content-Length", response.response().contentLength());
		}
		return response.asByteArray();
	}

	@Override
	public String getObjectAsString(String bucket, String key){
		return new String(getObjectAsBytes(bucket, key));
	}

	@Override
	public Optional<Long> length(String bucket, String key){
		return headObject(bucket, key)
				.map(HeadObjectResponse::contentLength);
	}

	@Override
	public Optional<Instant> findLastModified(String bucket, String key){
		return headObject(bucket, key)
				.map(HeadObjectResponse::lastModified);
	}

	@Override
	public Optional<S3Object> findLastModifiedObjectWithPrefix(String bucket, String prefix){
		return scanObjects(bucket, prefix)
				.findMax(Comparator.comparing(S3Object::lastModified));
	}

	@Override
	public URL generateLink(String bucket, String key, Duration expireAfter){
		return s3Presigner.presignGetObject(GetObjectPresignRequest.builder()
					.getObjectRequest(makeGetObjectRequest(bucket, key))
					.signatureDuration(expireAfter)
					.build())
				.url();
	}

	@Override
	public Scanner<List<S3Object>> scanObjectsPaged(String bucket, String prefix){
		var requestBuilder = ListObjectsV2Request.builder()
				.bucket(bucket)
				.prefix(prefix)
				.build();
		var paginator = getS3ClientForBucket(bucket).listObjectsV2Paginator(requestBuilder);
		return Scanner.of(paginator)
				.map(ListObjectsV2Response::contents);
	}

	@Override
	public Scanner<S3Object> scanObjects(String bucket, String prefix, String startAfter, String delimiter){
		var requestBuilder = ListObjectsV2Request.builder()
				.bucket(bucket);
		Optional.ofNullable(prefix).ifPresent(requestBuilder::prefix);
		Optional.ofNullable(startAfter).ifPresent(requestBuilder::startAfter);
		Optional.ofNullable(delimiter).ifPresent(requestBuilder::delimiter);
		var responsePages = getS3ClientForBucket(bucket).listObjectsV2Paginator(requestBuilder.build());
		return Scanner.of(responsePages)
				.concatIter(ListObjectsV2Response::contents);
	}

	@Override
	public Scanner<String> scanPrefixes(String bucket, String prefix, String startAfter, String delimiter){
		var requestBuilder = ListObjectsV2Request.builder()
				.bucket(bucket);
		Optional.ofNullable(prefix).ifPresent(requestBuilder::prefix);
		Optional.ofNullable(startAfter).ifPresent(requestBuilder::startAfter);
		Optional.ofNullable(delimiter).ifPresent(requestBuilder::delimiter);
		var responsePages = getS3ClientForBucket(bucket).listObjectsV2Paginator(requestBuilder.build());
		return Scanner.of(responsePages)
				.concatIter(ListObjectsV2Response::commonPrefixes)
				.map(CommonPrefix::prefix);
	}

	@Override
	public List<String> getCommonPrefixes(String bucket, String prefix, String delimiter){
		S3Client s3Client = getS3ClientForBucket(bucket);
		ListObjectsV2Request request = ListObjectsV2Request.builder()
				.bucket(bucket)
				.prefix(prefix)
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
	public Scanner<DirectoryDto> scanSubdirectories(String bucket, String prefix, String startAfter, String delimiter,
			int pageSize, boolean currentDirectory){
		ListObjectsV2Request request = ListObjectsV2Request.builder()
				.bucket(bucket)
				.prefix(prefix)
				.startAfter(startAfter)
				.delimiter(delimiter)
				.maxKeys(pageSize)
				.build();
		ListObjectsV2Iterable pages;
		try(var $ = TracerTool.startSpan("S3 listObjectsV2Paginator", TraceSpanGroupType.CLOUD_STORAGE)){
			pages = getS3ClientForBucket(bucket).listObjectsV2Paginator(request);
		}
		return Scanner.of(pages)
				.map(res -> {
					Scanner<DirectoryDto> objects = Scanner.of(res.contents())
							.map(object -> new DirectoryDto(
									object.key(),
									false,
									object.size(),
									object.lastModified(),
									object.storageClass().name()));
					Scanner<DirectoryDto> prefixes = Scanner.of(res.commonPrefixes())
							.map(commonPrefix -> new DirectoryDto(commonPrefix.prefix(), true, 0L, null, null));

					return Scanner.concat(objects, prefixes);
				})
				.concat(Function.identity());
	}

	@Override
	public boolean exists(String bucket, String key){
		return headObject(bucket, key).isPresent();
	}

	@Override
	public boolean existsPrefix(String bucket, String prefix){
		return scanObjects(bucket, prefix).hasAny();
	}

	private void putObjectWithAcl(String bucket, String key, ContentType contentType, Path path, ObjectCannedACL acl){
		S3Client s3Client = getS3ClientForBucket(bucket);
		PutObjectRequest request = makePutObjectRequestBuilder(bucket, key, contentType)
				.acl(acl)
				.build();
		RequestBody requestBody = RequestBody.fromFile(path);
		try(var $ = TracerTool.startSpan("S3 putObject", TraceSpanGroupType.CLOUD_STORAGE)){
			s3Client.putObject(request, requestBody);
			TracerTool.appendToSpanInfo("Content-Length", request.contentLength());
		}
	}

	private void handleTransfer(Transfer transfer, Runnable heartbeat){
		TransferProgress progress = transfer.getProgress();
		long totalBytesToTransfer = progress.getTotalBytesToTransfer();
		while(!transfer.isDone()){
			try{
				heartbeat.run();
			}catch(Exception e){
				logger.error("couldn't heartbeat", e);
			}
			logger.warn("{} / {} pct={} bytesTransferred={} totalBytesToTransfer={}", ByteUnitTool
					.byteCountToDisplaySize(progress.getBytesTransferred()), ByteUnitTool.byteCountToDisplaySize(
					totalBytesToTransfer), NumberFormatter.format(progress.getPercentTransferred(), 2), progress
					.getBytesTransferred(), totalBytesToTransfer);
			ThreadTool.sleepUnchecked(1000L);
		}
	}

	private Optional<HeadObjectResponse> headObject(String bucket, String key){
		try{
			S3Client s3Client = getS3ClientForBucket(bucket);
			HeadObjectRequest request = HeadObjectRequest.builder()
					.bucket(bucket)
					.key(key)
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

	private TransferManager getTransferManagerForBucket(String bucket){
		Region region = regionByBucket.computeIfAbsent(bucket, this::getBucketRegion);
		return transferManagerByRegion.computeIfAbsent(region, this::createTransferManager);
	}

	private S3Client getS3ClientForRegion(Region region){
		return s3ClientByRegion.computeIfAbsent(region, this::createClient);
	}

	private S3Client getS3ClientForBucket(String bucket){
		Region region = regionByBucket.computeIfAbsent(bucket, this::getBucketRegion);
		return getS3ClientForRegion(region);
	}

	@Override
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
							.firstMatchingHeader(Headers.S3_BUCKET_REGION)
							.get();
				}catch(S3Exception e2){
					region = e2.awsErrorDetails().sdkHttpResponse().firstMatchingHeader(Headers.S3_BUCKET_REGION).get();
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

	private TransferManager createTransferManager(Region region){
		return TransferManagerBuilder.standard()
				.withS3Client(AmazonS3ClientBuilder.standard()
						.withRegion(Regions.fromName(region.id()))
						.withCredentials(new AwsSdkV2ToV1CredentialsProvider(awsCredentialsProviderProvider.get()))
						.build())
				.build();
	}

	private static GetObjectRequest makeGetObjectRequest(String bucket, String key){
		return GetObjectRequest.builder()
				.bucket(bucket)
				.key(key)
				.build();
	}

	private static GetObjectRequest makeGetPartialObjectRequest(String bucket, String key, long offset, int length){
		long startInclusive = offset;
		long endInclusive = offset + length - 1;
		// https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.35
		// https://github.com/aws/aws-sdk-java-v2/issues/1472
		String range = "bytes=" + startInclusive + "-" + endInclusive;
		return GetObjectRequest.builder()
				.bucket(bucket)
				.key(key)
				.range(range)
				.build();
	}

	private static PutObjectRequest.Builder makePutObjectRequestBuilder(String bucket, String key,
			ContentType contentType){
		return PutObjectRequest.builder()
				.bucket(bucket)
				.key(key)
				.contentType(contentType.getMimeType());
	}

	private static PutObjectRequest makePutObjectRequest(String bucket, String key, ContentType contentType){
		return makePutObjectRequestBuilder(bucket, key, contentType).build();
	}

	private static void prepareFileDestination(Path path){
		try{
			Files.createDirectories(path.getParent());
			Files.deleteIfExists(path);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

}
