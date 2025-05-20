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
package io.datarouter.gcp.gcs.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.amazonaws.services.s3.AmazonS3;
import com.google.api.gax.paging.Page;
import com.google.auth.Credentials;
import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.Storage.BlobTargetOption;
import com.google.cloud.storage.Storage.BlobWriteOption;
import com.google.cloud.storage.Storage.CopyRequest;
import com.google.cloud.storage.Storage.PredefinedAcl;
import com.google.cloud.storage.StorageException;
import com.google.cloud.storage.StorageOptions;

import io.datarouter.bytes.ByteLength;
import io.datarouter.gcp.gcs.request.GcsMultipartRequests;
import io.datarouter.gcp.gcs.util.GcsHeaders.ContentType;
import io.datarouter.gcp.gcs.util.GcsPageScanner;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.file.BucketAndKey;
import io.datarouter.storage.file.BucketAndKeys;
import io.datarouter.storage.file.BucketAndPrefix;
import io.datarouter.storage.node.op.raw.read.DirectoryDto;
import io.datarouter.storage.node.op.raw.read.DirectoryScanner;
import io.datarouter.util.io.FilesTool;

public class GcsClient implements DirectoryScanner{

	//TODO can we reference this directly from GCS client code?
	private static final int FILE_NOT_FOUND_CODE = 404;

	private final Storage storage;
	private final GcsMultipartRequests multipartRequests;

	public GcsClient(Credentials credentials, AmazonS3 s3Client){
		// TODO check if should call getService lazily
		this.storage = StorageOptions.newBuilder()
				.setCredentials(credentials)
				.build()
				.getService();
		this.multipartRequests = new GcsMultipartRequests(s3Client);
	}

	/*--------- bucket ----------*/

	public Scanner<Bucket> scanBuckets(){
		Page<Bucket> buckets = storage.list();
		return Scanner.of(buckets.iterateAll());
	}

	public String getBucketLocation(String bucket){
		return storage.get(bucket).getLocation();
	}

	/*---------- read -----------*/

	public boolean exists(String bucket, String key){
		// TODO check Storage.BlobGetOption.fields()
		return storage.get(bucket, key) != null;
	}

	public Optional<Long> length(String bucket, String key){
		Blob blob = storage.get(bucket, key);
		return Optional.ofNullable(blob).map(Blob::getSize);
	}

	public InputStream getObject(String bucket, String key){
		try(var _ = TracerTool.startSpan("GCS getObject", TraceSpanGroupType.CLOUD_STORAGE)){
			ReadChannel reader = storage.reader(bucket, key);
			return Channels.newInputStream(reader);
		}
	}

	public Optional<byte[]> getObjectAsBytes(String bucket, String key){
		byte[] content;
		try(var _ = TracerTool.startSpan("GCS getObjectAsBytes", TraceSpanGroupType.CLOUD_STORAGE)){
			try{
				content = storage.readAllBytes(bucket, key);
				TracerTool.appendToSpanInfo("Content-Length", content.length);
				return Optional.of(content);
			}catch(StorageException storageException){
				if(storageException.getCode() == FILE_NOT_FOUND_CODE){
					return Optional.empty();
				}
				throw storageException;
			}
		}
	}

	public Optional<byte[]> getPartialObject(String bucket, String key, long offset, int length){
		try(var _ = TracerTool.startSpan("GCS getPartialObject", TraceSpanGroupType.CLOUD_STORAGE)){
			try{
				ReadChannel reader = storage.reader(bucket, key);
				reader.seek(offset);
				reader.setChunkSize(length);
				ByteBuffer bytes = ByteBuffer.allocate(length);
				reader.read(bytes);
				byte[] content = bytes.array();
				TracerTool.appendToSpanInfo("offset", offset);
				TracerTool.appendToSpanInfo("Content-Length", content.length);
				return Optional.of(content);
			}catch(StorageException storageException){
				if(storageException.getCode() == FILE_NOT_FOUND_CODE){
					return Optional.empty();
				}
				throw storageException;
			}
		}catch(IOException e){
			throw new RuntimeException("", e);
		}
	}

	public Optional<byte[]> findEnding(String bucket, String key, int length){
		try(var _ = TracerTool.startSpan("GCS readEnding", TraceSpanGroupType.CLOUD_STORAGE)){
			try{
				ReadChannel reader = storage.reader(bucket, key);
				reader.seek(-length);
				reader.setChunkSize(length);
				ByteBuffer byteBuffer = ByteBuffer.allocate(length);
				int numBytesRead = reader.read(byteBuffer);
				byte[] bytes = Arrays.copyOfRange(byteBuffer.array(), 0, numBytesRead);
				TracerTool.appendToSpanInfo("length", length);
				TracerTool.appendToSpanInfo("Content-Length", bytes.length);
				return Optional.of(bytes);
			}catch(StorageException storageException){
				if(storageException.getCode() == FILE_NOT_FOUND_CODE){
					return Optional.empty();
				}
				throw storageException;
			}
		}catch(IOException e){
			throw new RuntimeException("", e);
		}
	}

	/*---------- scan -----------*/

	@SuppressWarnings("resource")
	public Scanner<List<Blob>> scanObjectsPaged(String bucket, String prefix){
		BlobListOption blobListOption = BlobListOption.prefix(prefix);
		Page<Blob> firstPage = storage.list(bucket, blobListOption);
		return new GcsPageScanner<>(firstPage)
				.map(Page::getValues)
				.map(values -> Scanner.of(values)
						.list());
	}

	public Scanner<Blob> scanObjects(BucketAndPrefix bucketAndPrefix){
		return scanObjectsPaged(bucketAndPrefix.bucket(), bucketAndPrefix.prefix())
				.concat(Scanner::of);
	}

	@SuppressWarnings("resource")
	public Scanner<List<Blob>> scanObjectsPagedFrom(BucketAndPrefix bucketAndPrefix, String startKey){
		BlobListOption prefixOption = BlobListOption.prefix(bucketAndPrefix.prefix());
		BlobListOption fromOption = BlobListOption.startOffset(startKey);
		Page<Blob> firstPage = storage.list(bucketAndPrefix.bucket(), prefixOption, fromOption);
		return new GcsPageScanner<>(firstPage)
				.map(Page::getValues)
				.map(values -> Scanner.of(values).list());
	}

	public Scanner<Blob> scanObjectsFrom(BucketAndPrefix bucketAndPrefix, String startKey){
		return scanObjectsPagedFrom(bucketAndPrefix, startKey)
				.concat(Scanner::of);
	}

	/**
	 * @deprecated  Performance is unpredictable.  Use scanObjects(prefix) with client-side logic.
	 */
	@Deprecated
	@Override
	public Scanner<DirectoryDto> scanSubdirectories(
			String bucket,
			String prefix,
			String startAfter,
			String delimiter,
			int pageSize,
			boolean currentDirectory){
		List<BlobListOption> options = new ArrayList<>();
		if(prefix != null){
			options.add(BlobListOption.prefix(prefix));
		}
		if(startAfter != null){
			options.add(BlobListOption.startOffset(startAfter));
		}
		if(delimiter != null){
			options.add(BlobListOption.delimiter(delimiter));
		}
		options.add(BlobListOption.pageSize(pageSize));
		if(currentDirectory){
			options.add(BlobListOption.currentDirectory());
		}
		Page<Blob> pages = storage.list(bucket, options.toArray(BlobListOption[]::new));
		return Scanner.of(pages.iterateAll())
				.map(blob -> new DirectoryDto(
						blob.getName(),
						blob.isDirectory(),
						blob.getSize(),
						Optional.ofNullable(blob.getUpdateTime()).map(Instant::ofEpochMilli).orElse(null),
						Optional.ofNullable(blob.getStorageClass()).map(Object::toString).orElse(null)));
	}

	/*---------- write ----------*/

	public void putObjectAsBytes(
			String bucket,
			String key,
			ContentType contentType,
			String cacheControl,
			PredefinedAcl acl,
			byte[] bytes){
		BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucket, key))
				.setContentType(contentType.getMimeType())
				.setCacheControl(cacheControl)
				.build();
		BlobTargetOption[] blobTargetOptions = acl == null
				? new BlobTargetOption[]{}
				: new BlobTargetOption[]{BlobTargetOption.predefinedAcl(acl)};
		try(var _ = TracerTool.startSpan("GCS putObject", TraceSpanGroupType.CLOUD_STORAGE)){
			storage.create(blobInfo, bytes, blobTargetOptions);
			TracerTool.appendToSpanInfo("Content-Length", bytes.length);
		}
	}

	public void putInputStream(
			String bucket,
			String key,
			ContentType contentType,
			String cacheControl,
			PredefinedAcl acl,
			InputStream inputStream){
		BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucket, key))
				.setContentType(contentType.getMimeType())
				.setCacheControl(cacheControl)
				.build();
		try(var _ = TracerTool.startSpan("GCS putInputStream", TraceSpanGroupType.CLOUD_STORAGE)){
			try{
				storage.createFrom(
						blobInfo,
						inputStream,
						ByteLength.ofMiB(32).toBytesInt(),// Overrides default of 15 MiB
						BlobWriteOption.predefinedAcl(acl));
			}catch(IOException e){
				throw new UncheckedIOException("", e);
			}
		}
	}

	/*------- write multipart ----------*/

	public void multipartUpload(
			BucketAndKey location,
			ContentType contentType,
			InputStream inputStream,
			Threads threads,
			ByteLength minPartSize){
		multipartRequests.multipartUploadFromInputStream(
				location,
				contentType,
				Optional.empty(),
				inputStream,
				threads,
				minPartSize);
	}

	/*------- write from filesystem ----------*/

	public void putObject(String bucket, String key, ContentType contentType, Path path){
		putObjectWithAcl(bucket, key, contentType, path, PredefinedAcl.PRIVATE);
	}

	public void putPublicObject(String bucket, String key, ContentType contentType, Path path){
		putObjectWithAcl(bucket, key, contentType, path, PredefinedAcl.PUBLIC_READ);
	}

	// TODO streaming
	private void putObjectWithAcl(String bucket, String key, ContentType contentType, Path path, PredefinedAcl acl){
		BlobInfo blobInfo = makePutObjectRequest(bucket, key, contentType);
		try(var _ = TracerTool.startSpan("GCS putObject", TraceSpanGroupType.CLOUD_STORAGE)){
			storage.create(blobInfo, FilesTool.readAllBytes(path), BlobTargetOption.predefinedAcl(acl));
			TracerTool.appendToSpanInfo("Content-Length", blobInfo.getSize()); // todo check if we know the size
		}
	}

	private static BlobInfo makePutObjectRequest(String bucket, String key, ContentType contentType){
		return BlobInfo.newBuilder(BlobId.of(bucket, key))
				.setContentType(contentType.getMimeType())
				.build();
	}

	/*---------- copy -----------*/

	public void copyObject(String bucket, String sourceKey, String destinationKey, PredefinedAcl acl){
		CopyRequest request = CopyRequest.newBuilder()
				.setSource(bucket, sourceKey)
				.setTarget(BlobId.of(bucket, destinationKey), BlobTargetOption.predefinedAcl(acl))
				.build();
		try(var _ = TracerTool.startSpan("GCS copyObject", TraceSpanGroupType.CLOUD_STORAGE)){
			storage.copy(request).getResult();
		}
	}

	/*---------- delete -------------*/

	public void deleteObject(String bucket, String key){
		try(var _ = TracerTool.startSpan("GCS deleteObject", TraceSpanGroupType.CLOUD_STORAGE)){
			storage.delete(BlobId.of(bucket, key));
		}
	}

	public void deleteObjects(BucketAndKeys bucketAndKeys){
		List<BlobId> blobIds = Scanner.of(bucketAndKeys.keys())
				.map(key -> BlobId.of(bucketAndKeys.bucket(), key))
				.list();
		try(var _ = TracerTool.startSpan("GCS deleteObjects", TraceSpanGroupType.CLOUD_STORAGE)){
			storage.delete(blobIds);
		}
	}

}
