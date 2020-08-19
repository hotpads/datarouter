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
package io.datarouter.aws.s3.node;

import java.nio.charset.StandardCharsets;

import io.datarouter.aws.s3.DatarouterS3Client;
import io.datarouter.aws.s3.S3Headers;
import io.datarouter.aws.s3.S3Headers.ContentType;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.Require;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.S3Object;

public class S3DirectoryManager{

	private final DatarouterS3Client client;
	private final String bucket;
	private final String rootPath;

	public S3DirectoryManager(DatarouterS3Client client, String bucket, String rootPath){
		this.client = client;
		this.bucket = bucket;
		this.rootPath = rootPath;
	}

	/*-------------- util ----------------*/

	public String getBucket(){
		return bucket;
	}

	public String getRootPath(){
		return rootPath;
	}

	public String fullPath(String relativePath){
		return rootPath + relativePath;
	}

	public String relativePath(String fullPath){
		Require.isTrue(fullPath.startsWith(rootPath));
		return fullPath.substring(rootPath.length());
	}

	public S3DirectoryManager createSubdirectory(String name){
		return new S3DirectoryManager(
				client,
				bucket,
				rootPath + name + "/");
	}

	/*-------------- read ------------------*/

	public boolean exists(String relativePath){
		String fullPath = fullPath(relativePath);
		return client.exists(bucket, fullPath);
	}

	public byte[] read(String relativePath){
		String fullPath = fullPath(relativePath);
		return client.getObjectAsBytes(bucket, fullPath);
	}

	public String readUtf8(String relativePath){
		byte[] bytes = read(relativePath);
		return new String(bytes, StandardCharsets.UTF_8);
	}

	public Long size(String relativePath){
		String fullPath = fullPath(relativePath);
		return client.listObjects(bucket, fullPath)
				.findFirst()
				.map(S3Object::size)
				.orElse(null);
	}

	/*-------------- scan -----------------*/

	public Scanner<S3Object> scanS3Objects(){
		return client.listObjects(bucket, rootPath);
	}

	public Scanner<String> scanKeys(){
		return client.listObjects(bucket, rootPath)
				.map(S3Object::key)
				.map(this::relativePath);
	}

	/*------------ write -----------*/

	public void write(String relativePath, byte[] content){
		String fullPath = fullPath(relativePath);
		client.putObjectAsBytes(
				bucket,
				fullPath,
				ContentType.BINARY,
				S3Headers.CACHE_CONTROL_NO_CACHE,
				ObjectCannedACL.PRIVATE,
				content);
	}

	public void writeUtf8(String relativePath, String content){
		write(relativePath, content.getBytes(StandardCharsets.UTF_8));
	}

	/*------------ delete -------------*/

	public void delete(String relativePath){
		String fullPath = fullPath(relativePath);
		client.deleteObject(bucket, fullPath);
	}

}
