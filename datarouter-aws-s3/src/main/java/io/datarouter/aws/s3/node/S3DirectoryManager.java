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
package io.datarouter.aws.s3.node;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import io.datarouter.aws.s3.DatarouterS3Client;
import io.datarouter.aws.s3.S3Headers.ContentType;
import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.InputStreamAndLength;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.file.BucketAndKey;
import io.datarouter.storage.file.BucketAndPrefix;
import io.datarouter.storage.util.Subpath;
import io.datarouter.util.Require;
import software.amazon.awssdk.services.s3.model.S3Object;

public class S3DirectoryManager{

	private final DatarouterS3Client client;
	private final String bucket;
	private final Subpath rootPath;

	public S3DirectoryManager(DatarouterS3Client client, String bucket, Subpath rootPath){
		this.client = client;
		this.bucket = bucket;
		this.rootPath = rootPath;
	}

	/*-------------- util ----------------*/

	public String getBucket(){
		return bucket;
	}

	public Subpath getRootPath(){
		return rootPath;
	}

	public String fullPath(String suffix){
		return rootPath.toString() + suffix;
	}

	public String relativePath(String fullPath){
		Require.isTrue(fullPath.startsWith(rootPath.toString()));
		return fullPath.substring(rootPath.toString().length());
	}

	public S3DirectoryManager createSubdirectory(String name){
		return new S3DirectoryManager(
				client,
				bucket,
				rootPath.append(name));
	}

	/*-------------- read ------------------*/

	public boolean exists(String suffix){
		var location = new BucketAndKey(bucket, fullPath(suffix));
		return client.exists(location);
	}

	public Optional<Long> length(String suffix){
		var location = new BucketAndKey(bucket, fullPath(suffix));
		return client.length(location);
	}

	public byte[] read(String suffix){
		var location = new BucketAndKey(bucket, fullPath(suffix));
		return client.getObjectAsBytes(location);
	}

	public byte[] read(String suffix, long offset, int length){
		var location = new BucketAndKey(bucket, fullPath(suffix));
		return client.getPartialObject(location, offset, length);
	}

	public InputStream readInputStream(String suffix){
		var location = new BucketAndKey(bucket, fullPath(suffix));
		return client.getInputStream(location);
	}

	public Long size(String suffix){
		var locationPrefix = new BucketAndPrefix(bucket, fullPath(suffix));
		return client.scan(locationPrefix)
				.findFirst()
				.map(S3Object::size)
				.orElse(null);
	}

	/*-------------- scan -----------------*/

	public Scanner<List<S3Object>> scanS3ObjectsPaged(Subpath subpath){
		var location = new BucketAndPrefix(bucket, rootPath.append(subpath).toString());
		return client.scanPaged(location);
	}

	public Scanner<List<String>> scanKeysPaged(Subpath subpath){
		var location = new BucketAndPrefix(bucket, rootPath.append(subpath).toString());
		return client.scanPaged(location)
				.map(page -> Scanner.of(page)
						.map(S3Object::key)
						.map(this::relativePath)
						.list());
	}

	/*------------ write -----------*/

	public void write(String suffix, byte[] content){
		var location = new BucketAndKey(bucket, fullPath(suffix));
		client.putObject(
				location,
				ContentType.BINARY,
				content);
	}

	public void multipartUpload(String suffix, InputStream inputStream){
		var location = new BucketAndKey(bucket, fullPath(suffix));
		client.multipartUpload(location, ContentType.BINARY, inputStream);
	}

	public void multiThreadUpload(
			String suffix,
			InputStream inputStream,
			Threads threads,
			ByteLength minPartSize){
		var location = new BucketAndKey(bucket, fullPath(suffix));
		client.multithreadUpload(location, ContentType.BINARY, inputStream, threads, minPartSize);
	}

	public void multiThreadUpload(
			String suffix,
			Scanner<InputStreamAndLength> parts,
			Threads threads){
		var location = new BucketAndKey(bucket, fullPath(suffix));
		client.multithreadUpload(location, ContentType.BINARY, parts, threads);
	}

	/*------------ delete -------------*/

	public void delete(String suffix){
		var location = new BucketAndKey(bucket, fullPath(suffix));
		client.delete(location);
	}

}
