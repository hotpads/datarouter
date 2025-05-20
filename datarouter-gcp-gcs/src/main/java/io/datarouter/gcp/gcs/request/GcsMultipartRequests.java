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
package io.datarouter.gcp.gcs.request;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.google.cloud.storage.Storage.PredefinedAcl;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.KvString;
import io.datarouter.bytes.io.InputStreamAndLength;
import io.datarouter.bytes.io.InputStreamTool;
import io.datarouter.gcp.gcs.DatarouterGcsCounters;
import io.datarouter.gcp.gcs.DatarouterGcsCounters.GcsCounterSuffix;
import io.datarouter.gcp.gcs.util.GcsHeaders.ContentType;
import io.datarouter.gcp.gcs.util.GcsMultipartUploadPartSize;
import io.datarouter.gcp.gcs.util.GcsToAwsConstants;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.file.BucketAndKey;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.util.number.NumberFormatter;

public class GcsMultipartRequests{

	private static final Logger logger = LoggerFactory.getLogger(GcsMultipartRequests.class);

	private static final int FIRST_PART_NUMBER = 1;//parts range from 1 to 10,000

	private final AmazonS3 s3Client;

	public GcsMultipartRequests(AmazonS3 amazonS3){
		this.s3Client = amazonS3;
	}

	public void multipartUploadFromInputStream(
			BucketAndKey location,
			ContentType contentType,
			Optional<PredefinedAcl> optAcl,
			InputStream inputStream,
			Threads threads,
			ByteLength minPartSize){
		Scanner<InputStreamAndLength> parts = Scanner.iterate(FIRST_PART_NUMBER, partNumber -> partNumber + 1)
				.map(partNumber -> {
					int sizeForPart = Math.max(
							GcsMultipartUploadPartSize.sizeForPart(partNumber),
							minPartSize.toBytesInt());
					// These big buffer arrays could probably be reused to some degree.
					byte[] buffer = new byte[sizeForPart];
					int result = InputStreamTool.readUntilLength(inputStream, buffer, 0, buffer.length);
					int numRead = Math.max(result, 0);//remove potential -1
					return new InputStreamAndLength(
							new ByteArrayInputStream(buffer, 0, numRead),
							numRead);
				})
				.advanceWhile(inputStreamAndLength -> inputStreamAndLength.length() > 0);
		try{
			multipartUpload(location, contentType, optAcl, parts, threads);
		}finally{
			InputStreamTool.close(inputStream);
		}
	}

	/*---------- upload pre-cut parts ---------*/

	private record MultipartUploadPartNumberAndData(
			int number,
			InputStreamAndLength data){
	}

	public void multipartUpload(
			BucketAndKey location,
			ContentType contentType,
			Optional<PredefinedAcl> optAcl,
			Scanner<InputStreamAndLength> parts,
			Threads threads){
		var uploadId = createMultipartUploadRequest(location, contentType, optAcl);
		var partNumberTracker = new AtomicInteger(FIRST_PART_NUMBER);
		try{
			parts.map(inputStreamAndLength -> new MultipartUploadPartNumberAndData(
							partNumberTracker.getAndIncrement(),
							inputStreamAndLength))
					.parallelOrdered(threads)
					.map(part -> uploadPart(location, uploadId, part.number(), part.data()))
					.flush(completedParts -> completeMultipartUploadRequest(location, uploadId, completedParts));
		}catch(RuntimeException e){
			abortMultipartUploadRequest(location, uploadId);
			String message = String.format("Error on %s", location);
			throw new RuntimeException(message, e);
		}
	}

	/*----------- multipart sub-ops ----------*/

	public String createMultipartUploadRequest(
			BucketAndKey location,
			ContentType contentType,
			Optional<PredefinedAcl> optAcl){
		DatarouterGcsCounters.inc(location.bucket(), GcsCounterSuffix.MULTIPART_CREATE_REQUESTS, 1);
		var objectMetadata = new ObjectMetadata();
		objectMetadata.setContentType(contentType.toString());
		var createRequest = new InitiateMultipartUploadRequest(location.bucket(), location.key())
				.withObjectMetadata(objectMetadata);
		optAcl.ifPresent(acl -> createRequest.setCannedACL(GcsToAwsConstants.GCS_TO_S3_ACL_MAP.get(acl)));
		InitiateMultipartUploadResult initiateMultipartUploadResult;
		try(var _ = TracerTool.startSpan("GCS initiateMultipartUpload", TraceSpanGroupType.CLOUD_STORAGE)){
			initiateMultipartUploadResult = s3Client.initiateMultipartUpload(createRequest);
		}
		return initiateMultipartUploadResult.getUploadId();
	}

	public PartETag uploadPart(
			BucketAndKey location,
			String uploadId,
			int partNumber,
			InputStreamAndLength inputStreamAndLength){
		DatarouterGcsCounters.inc(location.bucket(), GcsCounterSuffix.MULTIPART_UPLOAD_REQUESTS, 1);
		var uploadPartRequest = new UploadPartRequest()
				.withBucketName(location.bucket())
				.withKey(location.key())
				.withUploadId(uploadId)
				.withPartNumber(partNumber)
				.withInputStream(inputStreamAndLength.inputStream())
				.withPartSize(inputStreamAndLength.length());
		UploadPartResult uploadPartResult;
		try(var _ = TracerTool.startSpan("GCS uploadPart", TraceSpanGroupType.CLOUD_STORAGE)){
			Instant startTime = Instant.now();
			uploadPartResult = s3Client.uploadPart(uploadPartRequest);
			Duration duration = Duration.between(startTime, Instant.now());
			TracerTool.appendToSpanInfo("Content-Length", uploadPartRequest.getPartSize());
			ByteLength size = ByteLength.ofBytes(inputStreamAndLength.length());
			double fractionalSeconds = duration.toMillis() / 1000d;
			ByteLength bytesPerSec = ByteLength.ofBytes((long)(size.toBytes() / fractionalSeconds));
			logger.info("Uploaded {}", new KvString()
					.add("location", location, BucketAndKey::toString)
					.add("partNumber", partNumber, NumberFormatter::addCommas)
					.add("size", size, ByteLength::toDisplay)
					.add("duration", new DatarouterDuration(duration), DatarouterDuration::toString)
					.add("rate", bytesPerSec.toDisplay() + "/s"));
		}
		DatarouterGcsCounters.inc(
				location.bucket(),
				GcsCounterSuffix.MULTIPART_UPLOAD_BYTES,
				inputStreamAndLength.length());

		return uploadPartResult.getPartETag();
	}

	public void completeMultipartUploadRequest(
			BucketAndKey location,
			String uploadId,
			List<PartETag> partETags){
		var completeRequest = new CompleteMultipartUploadRequest(
				location.bucket(),
				location.key(),
				uploadId,
				partETags);
		DatarouterGcsCounters.inc(location.bucket(), GcsCounterSuffix.MULTIPART_COMPLETE_REQUESTS, 1);
		try(var _ = TracerTool.startSpan("GCS completeMultipartUpload", TraceSpanGroupType.CLOUD_STORAGE)){
			s3Client.completeMultipartUpload(completeRequest);
		}
	}

	public void abortMultipartUploadRequest(
			BucketAndKey location,
			String uploadId){
		logger.warn("Aborting {}", location);
		DatarouterGcsCounters.inc(location.bucket(), GcsCounterSuffix.MULTIPART_ABORT_REQUESTS, 1);
		var abortRequest = new AbortMultipartUploadRequest(location.bucket(), location.key(), uploadId);
		try(var _ = TracerTool.startSpan("GCS abortMultipartUpload", TraceSpanGroupType.CLOUD_STORAGE)){
			s3Client.abortMultipartUpload(abortRequest);
		}
		logger.warn("Aborted {}", location);
	}

}
