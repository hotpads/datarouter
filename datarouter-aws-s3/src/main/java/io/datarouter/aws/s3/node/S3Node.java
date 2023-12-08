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
import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.io.InputStreamAndLength;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.file.BucketAndPrefix;
import io.datarouter.storage.file.DatabaseBlob;
import io.datarouter.storage.file.DatabaseBlob.DatabaseBlobFielder;
import io.datarouter.storage.file.DatabaseBlobKey;
import io.datarouter.storage.file.Pathbean;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.BlobStorage.PhysicalBlobStorageNode;
import io.datarouter.storage.node.op.raw.read.DirectoryDto;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.util.Subpath;

public class S3Node
extends BasePhysicalNode<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder>
implements PhysicalBlobStorageNode{

	private final DatarouterS3Client datarouterS3Client;
	private final S3DirectoryManager s3DirectoryManager;

	public S3Node(
			NodeParams<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> params,
			ClientType<?,?> clientType,
			DatarouterS3Client datarouterS3Client,
			S3DirectoryManager directoryManager){
		super(params, clientType);
		this.datarouterS3Client = datarouterS3Client;
		this.s3DirectoryManager = directoryManager;
	}

	public DatarouterS3Client getDatarouterS3Client(){
		return datarouterS3Client;
	}

	@Override
	public String getBucket(){
		return s3DirectoryManager.getBucket();
	}

	@Override
	public Subpath getRootPath(){
		return s3DirectoryManager.getRootPath();
	}

	@Override
	public boolean exists(PathbeanKey key, Config config){
		return s3DirectoryManager.exists(key.getPathAndFile());
	}

	@Override
	public Optional<Long> length(PathbeanKey key, Config config){
		return s3DirectoryManager.length(key.getPathAndFile());
	}

	@Override
	public Optional<byte[]> read(PathbeanKey key, Config config){
		return s3DirectoryManager.read(key.getPathAndFile());
	}

	@Override
	public Optional<byte[]> readPartial(PathbeanKey key, long offset, int length, Config config){
		return s3DirectoryManager.read(key.getPathAndFile(), offset, length);
	}

	@Override
	public InputStream readInputStream(PathbeanKey key, Config config){
		return s3DirectoryManager.readInputStream(key.getPathAndFile());
	}

	@Override
	public void write(PathbeanKey key, byte[] content, Config config){
		s3DirectoryManager.write(key.getPathAndFile(), content);
	}

	@Override
	public void writeInputStream(PathbeanKey key, InputStream inputStream, Config config){
		s3DirectoryManager.multipartUpload(key.getPathAndFile(), inputStream);
	}

	@Override
	public void writeParallel(
			PathbeanKey key,
			InputStream inputStream,
			Threads threads,
			ByteLength minPartSize,
			Config config){
		s3DirectoryManager.multiThreadUpload(key.getPathAndFile(), inputStream, threads, minPartSize);
	}

	@Override
	public void writeParallel(
			PathbeanKey key,
			Scanner<List<byte[]>> parts,
			Threads threads,
			Config config){
		s3DirectoryManager.multiThreadUpload(
				key.getPathAndFile(),
				parts.map(InputStreamAndLength::new),
				threads);
	}

	@Override
	public void delete(PathbeanKey key, Config config){
		s3DirectoryManager.delete(key.getPathAndFile());
	}

	@Override
	public void deleteMulti(List<PathbeanKey> keys, Config config){
		Scanner.of(keys)
				.map(PathbeanKey::getPathAndFile)
				.flush(s3DirectoryManager::deleteMulti);
	}

	@Override
	public void deleteAll(Subpath subpath, Config config){
		scanKeys(subpath).forEach(this::delete);
	}

	@Override
	public Scanner<List<Pathbean>> scanPaged(Subpath subpath, Config config){
		return s3DirectoryManager.scanS3ObjectsPaged(subpath)
				.map(page -> Scanner.of(page)
						.map(s3Object -> {
							PathbeanKey key = PathbeanKey.of(s3DirectoryManager.relativePath(s3Object.key()));
							Long size = s3Object.size();
							return new Pathbean(key, size);
						})
						.list());
	}

	@Override
	public Scanner<List<PathbeanKey>> scanKeysPaged(Subpath subpath, Config config){
		return s3DirectoryManager.scanKeysPaged(subpath)
				.map(page -> Scanner.of(page)
						.map(PathbeanKey::ofAllowEmptyFile)
						.list());
	}

	@Override
	public void vacuum(Config config){
		throw new UnsupportedOperationException();
	}

	@Override
	public Scanner<DirectoryDto> scanDirectories(BucketAndPrefix locationPrefix, String startAfter, int pageSize){
		return datarouterS3Client.scanSubdirectoriesOnly(locationPrefix, startAfter, FILE_PATH_DELIMITER,
				pageSize);
	}

	@Override
	public Scanner<DirectoryDto> scanFiles(BucketAndPrefix locationPrefix, String startAfter, int pageSize){
		return datarouterS3Client.scanFilesOnly(locationPrefix, startAfter, FILE_PATH_DELIMITER, pageSize);
	}
}
