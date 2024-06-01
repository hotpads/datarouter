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
package io.datarouter.gcp.gcs.node;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage.PredefinedAcl;

import io.datarouter.gcp.gcs.DatarouterGcsClient;
import io.datarouter.gcp.gcs.GcsHeaders;
import io.datarouter.gcp.gcs.GcsHeaders.ContentType;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.file.BucketAndKeys;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.util.Subpath;
import io.datarouter.util.Require;

public class GcsDirectoryManager{

	private final DatarouterGcsClient client;
	private final String bucket;
	private final Subpath rootPath;

	public GcsDirectoryManager(DatarouterGcsClient client, String bucket, Subpath rootPath){
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

	public GcsDirectoryManager createSubdirectory(String name){
		return new GcsDirectoryManager(
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

	public Optional<byte[]> read(PathbeanKey pathbeanKey){
		return read(pathbeanKey.getPathAndFile());
	}

	public Optional<byte[]> read(String suffix){
		String fullPath = fullPath(suffix);
		return client.getObjectAsBytes(bucket, fullPath);
	}

	public Optional<byte[]> read(String suffix, long offset, int length){
		String fullPath = fullPath(suffix);
		return client.getPartialObject(bucket, fullPath, offset, length);
	}

	public InputStream readInputStream(String suffix){
		String fullPath = fullPath(suffix);
		return client.getObject(bucket, fullPath);
	}

	public Long size(String suffix){
		String fullPath = fullPath(suffix);
		return client.scanObjectsPaged(bucket, fullPath)
				.concat(Scanner::of)
				.findFirst()
				.map(Blob::getSize)
				.orElse(null);
	}

	/*-------------- scan -----------------*/

	public Scanner<List<Blob>> scanGcsObjectsPaged(Subpath subpath){
		return client.scanObjectsPaged(bucket, rootPath.append(subpath).toString());
	}

	public Scanner<List<String>> scanKeysPaged(Subpath subpath){
		return client.scanObjectsPaged(bucket, rootPath.append(subpath).toString())
				.map(blobs -> Scanner.of(blobs)
						.map(Blob::getBlobId)
						.map(BlobId::getName)
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
				GcsHeaders.CACHE_CONTROL_NO_CACHE,
				PredefinedAcl.PRIVATE,
				content);
	}

	public void write(String suffix, InputStream inputStream){
		String fullPath = fullPath(suffix);
		client.putInputStream(
				bucket,
				fullPath,
				ContentType.BINARY,
				GcsHeaders.CACHE_CONTROL_NO_CACHE,
				PredefinedAcl.PRIVATE,
				inputStream);
	}

	public void writeUtf8(String suffix, String content){
		write(suffix, content.getBytes(StandardCharsets.UTF_8));
	}

	/*------------ delete -------------*/

	public void delete(String suffix){
		String fullPath = fullPath(suffix);
		client.deleteObject(bucket, fullPath);
	}

	public void deleteMulti(List<String> suffixes){
		BucketAndKeys bucketAndKeys = Scanner.of(suffixes)
				.map(this::fullPath)
				.listTo(keys -> new BucketAndKeys(bucket, keys));
		client.deleteObjects(bucketAndKeys);
	}

	public void deleteAll(Subpath subpath){
		scanKeysPaged(subpath)
				.concat(Scanner::of)
				.forEach(this::delete);
	}

}
