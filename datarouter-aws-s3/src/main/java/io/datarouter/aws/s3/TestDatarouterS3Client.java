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
package io.datarouter.aws.s3;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.inject.Singleton;

import io.datarouter.aws.s3.S3Headers.ContentType;
import io.datarouter.aws.s3.S3Headers.S3ContentType;
import io.datarouter.scanner.Scanner;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.S3Object;

@Singleton
public class TestDatarouterS3Client implements DatarouterS3Client{

	private final Path testFolder;

	public TestDatarouterS3Client(){
		try{
			this.testFolder = Files.createTempDirectory(null);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public Path getTestFolder(){
		return testFolder;
	}

	@Override
	public Scanner<Bucket> scanBuckets(){
		throw new UnsupportedOperationException();
	}

	@Override
	public Region getBucketRegion(String bucket){
		throw new UnsupportedOperationException();
	}

	@Override
	public void copyObject(String bucket, String sourceKey, String destinationKey, ObjectCannedACL acl){
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteObject(String bucket, String key){
		throw new UnsupportedOperationException();
	}

	@Override
	public void putObjectWithHeartbeat(String bucket, String key, ContentType contentType, Path path,
			Runnable heartbeat){
		throw new UnsupportedOperationException();
	}

	@Override
	public BufferedWriter putAsWriter(String bucket, String key, ContentType contentType){
		throw new UnsupportedOperationException();
	}

	@Override
	public OutputStream put(String bucket, String key, S3ContentType contentType){
		throw new UnsupportedOperationException();
	}

	@Override
	public void putObjectAsString(String bucket, String key, ContentType contentType, String content){
		throw new UnsupportedOperationException();
	}

	@Override
	public void putObjectAsBytes(String bucket, String key, ContentType contentType, String cacheControl,
			ObjectCannedACL acl, byte[] bytes){
		throw new UnsupportedOperationException();
	}

	@Override
	public void putPublicObject(String bucket, String key, ContentType contentType, Path path){
		throw new UnsupportedOperationException();
	}

	@Override
	public void putObject(String bucket, String key, ContentType contentType, Path path){
		Path destinationPath = testFolder.resolve(Path.of(bucket, key));
		try{
			Files.createDirectories(destinationPath.getParent());
			Files.copy(path, destinationPath);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	@Override
	public void downloadFilesToDirectory(String bucket, String prefix, Path path){
		throw new UnsupportedOperationException();
	}

	@Override
	public Path downloadFileToDirectory(String bucket, String key, Path path){
		throw new UnsupportedOperationException();
	}

	@Override
	public void downloadFile(String bucket, String key, Path path){
		throw new UnsupportedOperationException();
	}

	@Override
	public void downloadFileWithHeartbeat(String bucket, String key, Path path, Runnable heartbeat){
		throw new UnsupportedOperationException();
	}

	@Override
	public Scanner<List<String>> scanBatchesOfLinesWithPrefix(String bucket, String prefix, int batchSize){
		throw new UnsupportedOperationException();
	}

	@Override
	public Scanner<String> scanLines(String bucket, String key){
		throw new UnsupportedOperationException();
	}

	@Override
	public Scanner<List<String>> scanBatchesOfLines(String bucket, String key, int batchSize){
		throw new UnsupportedOperationException();
	}

	@Override
	public InputStream getObject(String bucket, String key){
		try{
			return Files.newInputStream(testFolder.resolve(Path.of(bucket, key)));
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] getObjectAsBytes(String bucket, String key){
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] getPartialObject(String bucket, String key, long offset, int length){
		throw new UnsupportedOperationException();
	}

	@Override
	public String getObjectAsString(String bucket, String key){
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<Instant> findLastModified(String bucket, String key){
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<S3Object> findLastModifiedObjectWithPrefix(String bucket, String prefix){
		throw new UnsupportedOperationException();
	}

	@Override
	public URL generateLink(String bucket, String key, Duration expireAfter){
		throw new UnsupportedOperationException();
	}

	@Override
	public Scanner<S3Object> listObjects(String bucket, String prefix){
		throw new UnsupportedOperationException();
	}

	@Override
	public Scanner<S3Object> scanObjects(String bucket, String prefix, String startAfter, String delimiter){
		throw new UnsupportedOperationException();
	}

	@Override
	public Scanner<String> scanPrefixes(String bucket, String prefix, String startAfter, String delimiter){
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> getCommonPrefixes(String bucket, String prefix, String delimiter){
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean exists(String bucket, String key){
		return false;
	}

	@Override
	public boolean existsPrefix(String bucket, String prefix){
		return false;
	}

}
