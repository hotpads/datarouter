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
package io.datarouter.filesystem.node.object;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.file.DatabaseBlob;
import io.datarouter.storage.file.DatabaseBlob.DatabaseBlobFielder;
import io.datarouter.storage.file.DatabaseBlobKey;
import io.datarouter.storage.file.Pathbean;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.BlobStorage.PhysicalBlobStorageNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.util.Subpath;

public class DirectoryBlobStorageNode
extends BasePhysicalNode<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder>
implements PhysicalBlobStorageNode{

	private final DirectoryBlobStorage directoryBlobStorage;
	//keep these for creating subdirectories
	private final String bucket;
	private final Subpath rootPath;

	public DirectoryBlobStorageNode(
			NodeParams<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> params,
			ClientType<?,?> clientType,
			DirectoryBlobStorage directoryBlobStorage,
			String bucket,
			Subpath rootPath){
		super(params, clientType);
		this.directoryBlobStorage = directoryBlobStorage;
		this.bucket = bucket;
		this.rootPath = rootPath;
	}

	@Override
	public String getBucket(){
		return bucket;
	}

	@Override
	public Subpath getRootPath(){
		return rootPath;
	}

	@Override
	public boolean exists(PathbeanKey key, Config config){
		return directoryBlobStorage.exists(key);
	}

	@Override
	public Optional<Long> length(PathbeanKey key, Config config){
		return directoryBlobStorage.length(key);
	}

	@Override
	public byte[] read(PathbeanKey key, Config config){
		return directoryBlobStorage.read(key);
	}

	@Override
	public byte[] read(PathbeanKey key, long offset, int length, Config config){
		return directoryBlobStorage.read(key, offset, length);
	}

	@Override
	public Map<PathbeanKey,byte[]> read(List<PathbeanKey> keys, Config config){
		return Scanner.of(keys)
				.toMap(Function.identity(), directoryBlobStorage::read);
	}

	@Override
	public InputStream readInputStream(PathbeanKey key, Config config){
		return directoryBlobStorage.readInputStream(key);
	}

	@Override
	public void write(PathbeanKey key, byte[] content, Config config){
		directoryBlobStorage.write(key, content);
	}

	@Override
	public void write(PathbeanKey key, InputStream inputStream, Config config){
		directoryBlobStorage.write(key, inputStream);
	}

	@Override
	public Scanner<List<Pathbean>> scanPaged(Subpath subpath, Config config){
		//sorted should be true except when findAllowUnsortedScan is present and true
		boolean sorted = !config.findAllowUnsortedScan().orElse(false);
		return directoryBlobStorage.scanPaged(subpath, sorted);
	}

	@Override
	public Scanner<List<PathbeanKey>> scanKeysPaged(Subpath subpath, Config config){
		//sorted should be true except when findAllowUnsortedScan is present and true
		boolean sorted = !config.findAllowUnsortedScan().orElse(false);
		return directoryBlobStorage.scanKeysPaged(subpath, sorted);
	}

	@Override
	public void delete(PathbeanKey key, Config config){
		directoryBlobStorage.delete(key);
	}

	@Override
	public void deleteAll(Subpath subpath, Config config){
		directoryBlobStorage.deleteAll(subpath);
	}

	@Override
	public void vacuum(Config config){
		throw new UnsupportedOperationException();
	}

}
