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

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import io.datarouter.aws.s3.DatarouterS3Client;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.file.Pathbean;
import io.datarouter.storage.file.Pathbean.PathbeanFielder;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.BlobStorage.PhysicalBlobStorageNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.util.Subpath;

public class S3Node
extends BasePhysicalNode<PathbeanKey,Pathbean,PathbeanFielder>
implements PhysicalBlobStorageNode{

	private final DatarouterS3Client datarouterS3Client;
	private final S3DirectoryManager s3DirectoryManager;

	public S3Node(
			NodeParams<PathbeanKey,Pathbean,PathbeanFielder> params,
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
	public Optional<Long> length(PathbeanKey key){
		return s3DirectoryManager.length(key.getPathAndFile());
	}

	@Override
	public byte[] read(PathbeanKey key){
		return s3DirectoryManager.read(key.getPathAndFile());
	}

	@Override
	public byte[] read(PathbeanKey key, long offset, int length){
		return s3DirectoryManager.read(key.getPathAndFile(), offset, length);
	}

	@Override
	public Map<PathbeanKey,byte[]> read(List<PathbeanKey> keys){
		return Scanner.of(keys)
				.toMap(Function.identity(), this::read);
	}

	@Override
	public void write(PathbeanKey key, byte[] content, Config config){
		s3DirectoryManager.write(key.getPathAndFile(), content);
	}

	@Override
	public void write(PathbeanKey key, Scanner<byte[]> chunks){
		s3DirectoryManager.write(key.getPathAndFile(), chunks);
	}

	@Override
	public void write(PathbeanKey key, InputStream inputStream){
		s3DirectoryManager.write(key.getPathAndFile(), inputStream);
	}

	@Override
	public void delete(PathbeanKey key){
		s3DirectoryManager.delete(key.getPathAndFile());
	}

	@Override
	public void deleteAll(Subpath subpath){
		scanKeys(subpath).forEach(this::delete);
	}

	@Override
	public boolean exists(PathbeanKey key){
		return s3DirectoryManager.exists(key.getPathAndFile());
	}

	@Override
	public Scanner<List<Pathbean>> scanPaged(Subpath subpath){
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
	public Scanner<List<PathbeanKey>> scanKeysPaged(Subpath subpath){
		return s3DirectoryManager.scanKeysPaged(subpath)
				.map(page -> Scanner.of(page)
						.map(PathbeanKey::of)
						.list());
	}

}
