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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.aws.s3.DatarouterS3ClientManager;
import io.datarouter.aws.s3.DatarouterS3Counters;
import io.datarouter.aws.s3.DatarouterS3Counters.S3CounterSuffix;
import io.datarouter.aws.s3.MultipartUploadPartSize;
import io.datarouter.aws.s3.S3CostCounters;
import io.datarouter.aws.s3.S3Headers.S3ContentType;
import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.KvString;
import io.datarouter.bytes.io.ExposedByteArrayOutputStream;
import io.datarouter.bytes.io.InputStreamAndLength;
import io.datarouter.bytes.io.InputStreamTool;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.file.BucketAndKey;
import io.datarouter.util.Require;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.util.number.NumberFormatter;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

public class DatarouterS3MultipartRequests{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterS3MultipartRequests.class);

	// With S3 limit of 10_000 parts, this limits file size to 320 GiB
	private static final int MIN_UPLOAD_PART_SIZE_BYTES = ByteLength.ofMiB(32).toBytesInt();
	private static final int FIRST_PART_NUMBER = 1;//s3 partNumbers start at 1

	private final DatarouterS3ClientManager clientManager;

	public DatarouterS3MultipartRequests(DatarouterS3ClientManager clientManager){
		this.clientManager = clientManager;
	}

	/**
	 * @deprecated  Use the InputStream based methods
	 */
	@Deprecated
	public OutputStream multipartUploadOutputStream(
			BucketAndKey location,
			S3ContentType contentType){
		S3Client s3Client = clientManager.getS3ClientForBucket(location.bucket());
		String uploadId = createMultipartUploadRequest(location, contentType, Optional.empty());
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
				try(var _ = TracerTool.startSpan("S3 uploadPartFromOutputStream", TraceSpanGroupType.CLOUD_STORAGE)){
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

	/*---------- convert InputStream into parts ---------*/

	public void multipartUploadFromInputStream(
			BucketAndKey location,
			S3ContentType contentType,
			Optional<ObjectCannedACL> optAcl,
			InputStream inputStream,
			Threads threads,
			ByteLength minPartSize){
		Scanner<InputStreamAndLength> parts = Scanner.iterate(FIRST_PART_NUMBER, partNumber -> partNumber + 1)
				.map(partNumber -> {
					int sizeForPart = Math.max(
							MultipartUploadPartSize.sizeForPart(partNumber),
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

	private void multipartUpload(
			BucketAndKey location,
			S3ContentType contentType,
			Optional<ObjectCannedACL> optAcl,
			Scanner<InputStreamAndLength> parts,
			Threads threads){
		String uploadId = createMultipartUploadRequest(location, contentType, optAcl);
		var partNumberTracker = new AtomicInteger(FIRST_PART_NUMBER);
		try{
			parts
					.map(inputStreamAndLength -> new MultipartUploadPartNumberAndData(
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

	/*----------- multipart sub-ops ----------*/

	public String createMultipartUploadRequest(
			BucketAndKey location,
			S3ContentType contentType,
			Optional<ObjectCannedACL> optAcl){
		DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.MULTIPART_CREATE_REQUESTS, 1);
		CreateMultipartUploadRequest createRequest = CreateMultipartUploadRequest.builder()
				.acl(optAcl.orElse(null))
				.bucket(location.bucket())
				.key(location.key())
				.contentType(contentType.getMimeType())
				.build();
		CreateMultipartUploadResponse createResponse;
		S3Client s3Client = clientManager.getS3ClientForBucket(location.bucket());
		try(var _ = TracerTool.startSpan("S3 createMultipartUpload", TraceSpanGroupType.CLOUD_STORAGE)){
			createResponse = s3Client.createMultipartUpload(createRequest);
		}
		S3CostCounters.write();// Cost uncertain
		return createResponse.uploadId();
	}

	public CompletedPart uploadPart(
			BucketAndKey location,
			String uploadId,
			int partNumber,
			InputStreamAndLength inputStreamAndLength){
		DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.MULTIPART_UPLOAD_REQUESTS, 1);
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
		try(var _ = TracerTool.startSpan("S3 uploadPart", TraceSpanGroupType.CLOUD_STORAGE)){
			Instant startTime = Instant.now();
			uploadPartResponse = s3Client.uploadPart(uploadPartRequest, requestBody);
			Duration duration = Duration.between(startTime, Instant.now());
			TracerTool.appendToSpanInfo("Content-Length", uploadPartRequest.contentLength());
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
		DatarouterS3Counters.inc(
				location.bucket(),
				S3CounterSuffix.MULTIPART_UPLOAD_BYTES,
				inputStreamAndLength.length());
		S3CostCounters.write();
		return CompletedPart.builder()
				.partNumber(partNumber)
				.eTag(uploadPartResponse.eTag())
				.build();
	}

	public void completeMultipartUploadRequest(
			BucketAndKey location,
			String uploadId,
			List<CompletedPart> completedParts){
		DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.MULTIPART_COMPLETE_REQUESTS, 1);
		List<CompletedPart> allCompletedParts = new ArrayList<>(completedParts);
		if(completedParts.isEmpty()){
			// Need to provide an empty part or S3 will error
			CompletedPart emptyPart = uploadPart(
					location,
					uploadId,
					FIRST_PART_NUMBER,
					new InputStreamAndLength(List.of()));
			allCompletedParts.add(emptyPart);
		}
		CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
				// CompleteMultipartUploadRequest requires the parts be sorted
				.parts(Scanner.of(allCompletedParts)
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
		try(var _ = TracerTool.startSpan("S3 completeMultipartUpload", TraceSpanGroupType.CLOUD_STORAGE)){
			s3Client.completeMultipartUpload(completeMultipartUploadRequest);
		}
		S3CostCounters.write();// Cost uncertain
	}

	public void abortMultipartUploadRequest(
			BucketAndKey location,
			String uploadId){
		logger.warn("Aborting {}", location);
		DatarouterS3Counters.inc(location.bucket(), S3CounterSuffix.MULTIPART_ABORT_REQUESTS, 1);
		AbortMultipartUploadRequest abortRequest = AbortMultipartUploadRequest.builder()
				.bucket(location.bucket())
				.key(location.key())
				.uploadId(uploadId)
				.build();
		S3Client s3Client = clientManager.getS3ClientForBucket(location.bucket());
		try(var _ = TracerTool.startSpan("S3 abortMultipartUpload", TraceSpanGroupType.CLOUD_STORAGE)){
			s3Client.abortMultipartUpload(abortRequest);
		}
		S3CostCounters.write();// Cost uncertain
		logger.warn("Aborted {}", location);
	}

}
