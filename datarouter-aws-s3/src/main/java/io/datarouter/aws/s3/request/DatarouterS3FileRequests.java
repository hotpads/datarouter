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

import java.nio.file.Path;

import io.datarouter.aws.s3.DatarouterS3ClientManager;
import io.datarouter.aws.s3.DatarouterS3Counters;
import io.datarouter.aws.s3.DatarouterS3Counters.S3CounterSuffix;
import io.datarouter.aws.s3.S3Headers.ContentType;
import io.datarouter.aws.s3.S3Tool;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.storage.file.BucketAndKey;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class DatarouterS3FileRequests{

	private final DatarouterS3ClientManager clientManager;

	public DatarouterS3FileRequests(DatarouterS3ClientManager clientManager){
		this.clientManager = clientManager;
	}

	/*------------- read ------------*/

	public void downloadToLocalFile(BucketAndKey location, Path localFilePath){
		S3Tool.prepareLocalFileDestination(localFilePath);
		S3Client s3Client = clientManager.getS3ClientForBucket(location.bucket());
		GetObjectRequest request = GetObjectRequest.builder()
				.bucket(location.bucket())
				.key(location.key())
				.build();
		GetObjectResponse response;
		try(var $ = TracerTool.startSpan("S3 downloadToLocalFile", TraceSpanGroupType.CLOUD_STORAGE)){
			response = s3Client.getObject(request, ResponseTransformer.toFile(localFilePath));
			TracerTool.appendToSpanInfo("Content-Length", response.contentLength());
		}
		DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.DOWNLOAD_FILE_REQUESTS, 1);
		DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.DOWNLOAD_FILE_BYTES, response.contentLength());
	}

	/*------------- write ------------*/

	public void uploadLocalFile(BucketAndKey location, ContentType contentType, Path localFilePath){
		S3Client s3Client = clientManager.getS3ClientForBucket(location.bucket());
		PutObjectRequest request = PutObjectRequest.builder()
				.bucket(location.bucket())
				.key(location.key())
				.contentType(contentType.getMimeType())
				.acl(ObjectCannedACL.PRIVATE)
				.build();
		RequestBody requestBody = RequestBody.fromFile(localFilePath);
		try(var $ = TracerTool.startSpan("S3 uploadLocalFile", TraceSpanGroupType.CLOUD_STORAGE)){
			s3Client.putObject(request, requestBody);
			TracerTool.appendToSpanInfo("Content-Length", request.contentLength());
		}
		DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.UPLOAD_FILE_REQUESTS, 1);
		// TODO non-deprecated alternative to requestBody.contentLength()
		DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.UPLOAD_FILE_BYTES, requestBody.contentLength());
	}

}
