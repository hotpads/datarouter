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
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import io.datarouter.aws.s3.DatarouterS3ClientManager;
import io.datarouter.aws.s3.DatarouterS3Counters;
import io.datarouter.aws.s3.DatarouterS3Counters.S3CounterSuffix;
import io.datarouter.aws.s3.S3CostCounters;
import io.datarouter.aws.s3.S3Headers.ContentType;
import io.datarouter.aws.s3.S3Headers.S3ContentType;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.storage.file.BucketAndKey;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

public class DatarouterS3PublicRequests{

	private final DatarouterS3ClientManager clientManager;
	private final DatarouterS3MultipartRequests multipartRequests;

	public DatarouterS3PublicRequests(DatarouterS3ClientManager clientManager){
		this.clientManager = clientManager;
		multipartRequests = new DatarouterS3MultipartRequests(clientManager);
	}

	/*---------- link ----------*/

	public URL generateLink(BucketAndKey location, Duration expireAfter){
		DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.GENERATE_LINK_REQUESTS, 1);
		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
				.bucket(location.bucket())
				.key(location.key())
				.build();
		GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
				.getObjectRequest(getObjectRequest)
				.signatureDuration(expireAfter)
				.build();
		S3CostCounters.write();
		try(var $ = TracerTool.startSpan("S3 generateLink", TraceSpanGroupType.CLOUD_STORAGE)){
			return clientManager.getPresigner().presignGetObject(getObjectPresignRequest).url();
		}
	}

	/*--------- write bytes ---------*/

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
		try(var $ = TracerTool.startSpan("S3 putObjectWithPublicRead", TraceSpanGroupType.CLOUD_STORAGE)){
			s3Client.putObject(request, requestBody);
			TracerTool.appendToSpanInfo("Content-Length", request.contentLength());
		}
		DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.WRITE_OBJECT_REQUESTS, 1);
		DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.WRITE_OBJECT_BYTES, bytes.length);
		S3CostCounters.write();
	}

	public void putObjectWithPublicReadAndExpirationTime(
			BucketAndKey location,
			ContentType contentType,
			String cacheControl,
			byte[] bytes,
			Instant expirationTime){
		S3Client s3Client = clientManager.getS3ClientForBucket(location.bucket());
		PutObjectRequest request = PutObjectRequest.builder()
				.bucket(location.bucket())
				.key(location.key())
				.contentType(contentType.getMimeType())
				.cacheControl(cacheControl)
				.acl(ObjectCannedACL.PUBLIC_READ)
				.expires(expirationTime)
				.build();
		RequestBody requestBody = RequestBody.fromBytes(bytes);
		try(var $ = TracerTool.startSpan(
				"S3 putObjectWithPublicReadAndExpirationTime",
				TraceSpanGroupType.CLOUD_STORAGE)){
			s3Client.putObject(request, requestBody);
			TracerTool.appendToSpanInfo("Content-Length", request.contentLength());
		}
		DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.WRITE_OBJECT_REQUESTS, 1);
		DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.WRITE_OBJECT_BYTES, bytes.length);
		S3CostCounters.write();
	}

	/*--------- write multipart --------*/

	public void multipartUploadWithPublicRead(
			BucketAndKey location,
			S3ContentType contentType,
			InputStream inputStream){
		multipartRequests.multipartUploadInternal(
				location,
				contentType,
				Optional.of(ObjectCannedACL.PUBLIC_READ),
				inputStream);
	}

	/*--------- write file ---------*/

	public void uploadLocalFileWithPublicRead(BucketAndKey location, ContentType contentType, Path localFilePath){
		S3Client s3Client = clientManager.getS3ClientForBucket(location.bucket());
		PutObjectRequest request = PutObjectRequest.builder()
				.bucket(location.bucket())
				.key(location.key())
				.contentType(contentType.getMimeType())
				.acl(ObjectCannedACL.PUBLIC_READ)
				.build();
		RequestBody requestBody = RequestBody.fromFile(localFilePath);
		try(var $ = TracerTool.startSpan("S3 uploadLocalFileWithPublicRead", TraceSpanGroupType.CLOUD_STORAGE)){
			s3Client.putObject(request, requestBody);
			TracerTool.appendToSpanInfo("Content-Length", request.contentLength());
		}
		DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.UPLOAD_FILE_REQUESTS, 1);
		// TODO non-deprecated alternative to requestBody.contentLength()
		DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.UPLOAD_FILE_BYTES, requestBody.contentLength());
		S3CostCounters.write();
	}

}
