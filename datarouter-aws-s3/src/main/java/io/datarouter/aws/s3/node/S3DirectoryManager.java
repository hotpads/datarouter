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
import java.util.concurrent.ExecutorService;

import io.datarouter.aws.s3.DatarouterS3Client;
import io.datarouter.aws.s3.S3Headers;
import io.datarouter.aws.s3.S3Headers.ContentType;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.util.Subpath;
import io.datarouter.util.Require;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
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
		String fullPath = fullPath(suffix);
		return client.exists(bucket, fullPath);
	}

	public Optional<Long> length(String suffix){
		String fullPath = fullPath(suffix);
		return client.length(bucket, fullPath);
	}

	public byte[] read(String suffix){
		String fullPath = fullPath(suffix);
		return client.getObjectAsBytes(bucket, fullPath);
	}

	public byte[] read(String suffix, long offset, int length){
		String fullPath = fullPath(suffix);
		return client.getPartialObject(bucket, fullPath, offset, length);
	}

	public InputStream readInputStream(String suffix){
		return client.getObject(bucket, fullPath(suffix));
	}

	public Long size(String suffix){
		String fullPath = fullPath(suffix);
		return client.scanObjects(bucket, fullPath)
				.findFirst()
				.map(S3Object::size)
				.orElse(null);
	}

	/*-------------- scan -----------------*/

	public Scanner<List<S3Object>> scanS3ObjectsPaged(Subpath subpath){
		return client.scanObjectsPaged(bucket, rootPath.append(subpath).toString());
	}

	public Scanner<List<String>> scanKeysPaged(Subpath subpath){
		return client.scanObjectsPaged(bucket, rootPath.append(subpath).toString())
				.map(page -> Scanner.of(page)
						.map(S3Object::key)
						.map(this::relativePath)
						.list());
	}

	/*------------ write -----------*/

	public void write(String suffix, byte[] content){
		String fullPath = fullPath(suffix);
		client.putObjectAsBytes(
				bucket,
				fullPath,
				ContentType.BINARY,
				S3Headers.CACHE_CONTROL_NO_CACHE,
				ObjectCannedACL.PRIVATE,
				content);
	}

	public void multipartUpload(String suffix, InputStream inputStream){
		String fullPath = fullPath(suffix);
		client.multipartUpload(bucket, fullPath, ContentType.BINARY, inputStream);
	}

	public void multiThreadUpload(
			String suffix,
			InputStream inputStream,
			ExecutorService exec,
			int numThreads){
		String fullPath = fullPath(suffix);
		client.multiThreadUpload(bucket, fullPath, ContentType.BINARY, inputStream, exec, numThreads);
	}

	/*------------ delete -------------*/

	public void delete(String suffix){
		String fullPath = fullPath(suffix);
		client.deleteObject(bucket, fullPath);
	}

}
