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

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import io.datarouter.aws.s3.DatarouterS3ClientManager;
import io.datarouter.aws.s3.DatarouterS3Counters;
import io.datarouter.aws.s3.DatarouterS3Counters.S3CounterSuffix;
import io.datarouter.aws.s3.S3CostCounters;
import io.datarouter.aws.s3.S3Headers.S3ContentType;
import io.datarouter.aws.s3.S3Limits;
import io.datarouter.aws.s3.S3Tool;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.file.BucketAndKey;
import io.datarouter.storage.file.BucketAndKeys;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class DatarouterS3ObjectRequests{

	private final DatarouterS3ClientManager clientManager;

	public DatarouterS3ObjectRequests(DatarouterS3ClientManager clientManager){
		this.clientManager = clientManager;
	}

	/*--------- head ---------*/

	public Optional<HeadObjectResponse> head(BucketAndKey location){
		DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.HEAD_REQUESTS, 1);
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
			DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.HEAD_HIT, 1);
			S3CostCounters.read();
			return Optional.of(response);
		}catch(NoSuchKeyException e){
			DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.HEAD_MISS, 1);
			return Optional.empty();
		}
	}

	/*---------- read bytes ---------*/

	public ResponseInputStream<GetObjectResponse> getResponseInputStream(BucketAndKey location){
		S3Client s3Client = clientManager.getS3ClientForBucket(location.bucket());
		GetObjectRequest request = GetObjectRequest.builder()
				.bucket(location.bucket())
				.key(location.key())
				.build();
		ResponseInputStream<GetObjectResponse> response;
		try(var $ = TracerTool.startSpan("S3 getObjectInputStream", TraceSpanGroupType.CLOUD_STORAGE)){
			response = s3Client.getObject(request);
			TracerTool.appendToSpanInfo("Content-Length", response.response().contentLength());
		}
		DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.READ_INPUT_STREAM_REQUESTS, 1);
		S3CostCounters.read();
		// TODO add CountingInputStream to count bytes
		return response;
	}

	public InputStream getInputStream(BucketAndKey location){
		return getResponseInputStream(location);
	}

	public Optional<byte[]> findObject(BucketAndKey location){
		S3Client s3Client = clientManager.getS3ClientForBucket(location.bucket());
		GetObjectRequest request = GetObjectRequest.builder()
				.bucket(location.bucket())
				.key(location.key())
				.build();
		DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.READ_OBJECT_REQUESTS, 1);
		S3CostCounters.read();
		ResponseBytes<GetObjectResponse> response;
		try{
			try(var $ = TracerTool.startSpan("S3 getObjectAsBytes", TraceSpanGroupType.CLOUD_STORAGE)){
				response = s3Client.getObjectAsBytes(request);
				TracerTool.appendToSpanInfo("Content-Length", response.response().contentLength());
			}
			byte[] bytes = response.asByteArray();
			DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.READ_OBJECT_HIT, 1);
			DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.READ_OBJECT_BYTES, bytes.length);
			return Optional.of(bytes);
		}catch(NoSuchKeyException e){
			DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.READ_OBJECT_MISS, 1);
			return Optional.empty();
		}
	}

	public Optional<byte[]> findPartialObject(BucketAndKey location, long offset, int length){
		S3Client s3Client = clientManager.getS3ClientForBucket(location.bucket());
		String rangeParam = S3Tool.makeGetPartialObjectRangeParam(offset, length);
		GetObjectRequest request = GetObjectRequest.builder()
				.bucket(location.bucket())
				.key(location.key())
				.range(rangeParam)
				.build();
		DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.READ_PARTIAL_REQUESTS, 1);
		S3CostCounters.read();
		ResponseBytes<GetObjectResponse> response;
		try{
			try(var $ = TracerTool.startSpan("S3 getPartialObject", TraceSpanGroupType.CLOUD_STORAGE)){
				response = s3Client.getObjectAsBytes(request);
				TracerTool.appendToSpanInfo("offset", offset);
				TracerTool.appendToSpanInfo("Content-Length", response.response().contentLength());
			}
			byte[] bytes = response.asByteArray();
			DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.READ_PARTIAL_HIT, 1);
			DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.READ_PARTIAL_BYTES, bytes.length);
			return Optional.of(bytes);
		}catch(NoSuchKeyException e){
			DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.READ_PARTIAL_MISS, 1);
			return Optional.empty();
		}
	}

	/*---------- write bytes ---------*/

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
		DatarouterS3Counters.inc(bucket, S3CounterSuffix.COPY_REQUESTS, 1);
		S3CostCounters.write();
	}

	public void putObject(
			BucketAndKey location,
			S3ContentType contentType,
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
		DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.WRITE_OBJECT_REQUESTS, 1);
		DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.WRITE_OBJECT_BYTES, bytes.length);
		S3CostCounters.write();
	}

	/*---------- delete ----------*/

	public void delete(BucketAndKey location){
		DeleteObjectRequest request = DeleteObjectRequest.builder()
				.bucket(location.bucket())
				.key(location.key())
				.build();
		S3Client s3Client = clientManager.getS3ClientForBucket(location.bucket());
		try(var $ = TracerTool.startSpan("S3 deleteObject", TraceSpanGroupType.CLOUD_STORAGE)){
			s3Client.deleteObject(request);
		}
		DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.DELETE_REQUESTS, 1);
		// Cost: S3 deletes are free
	}

	public void deleteMulti(BucketAndKeys bucketAndKeys){
		S3Client s3Client = clientManager.getS3ClientForBucket(bucketAndKeys.bucket());
		Scanner.of(bucketAndKeys.keys())
				// Ideally the caller would have already batched, but in case they didn't
				.batch(S3Limits.MAX_DELETE_MULTI_KEYS)
				.forEach(keyBatch -> {
					List<ObjectIdentifier> objectIdentifiers = Scanner.of(keyBatch)
							.map(key -> ObjectIdentifier.builder().key(key).build())
							.list();
					DeleteObjectsRequest request = DeleteObjectsRequest.builder()
							.bucket(bucketAndKeys.bucket())
							.delete(Delete.builder().objects(objectIdentifiers).build())
							.build();
					try(var $ = TracerTool.startSpan("S3 deleteObjects", TraceSpanGroupType.CLOUD_STORAGE)){
						s3Client.deleteObjects(request);
						TracerTool.appendToSpanInfo("deleted", objectIdentifiers.size());
					}
		});
		DatarouterS3Counters.inc(
				bucketAndKeys.bucket(),
				S3CounterSuffix.DELETE_MULTI_REQUESTS,
				1);
		DatarouterS3Counters.inc(
				bucketAndKeys.bucket(),
				S3CounterSuffix.DELETE_MULTI_KEYS,
				bucketAndKeys.keys().size());
		// Cost: S3 deletes are free
	}

}
