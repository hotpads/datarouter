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
import java.util.List;
import java.util.Optional;

import io.datarouter.gcp.gcs.DatarouterGcsClient;
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

public class GcsNode
extends BasePhysicalNode<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder>
implements PhysicalBlobStorageNode{

	private final DatarouterGcsClient datarouterGcsClient;
	private final GcsDirectoryManager gcsDirectoryManager;

	public GcsNode(
			NodeParams<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> params,
			ClientType<?,?> clientType,
			DatarouterGcsClient datarouterGcsClient,
			GcsDirectoryManager directoryManager){
		super(params, clientType);
		this.datarouterGcsClient = datarouterGcsClient;
		this.gcsDirectoryManager = directoryManager;
	}

	public DatarouterGcsClient getDatarouterGcsClient(){
		return datarouterGcsClient;
	}

	@Override
	public String getBucket(){
		return gcsDirectoryManager.getBucket();
	}

	@Override
	public Subpath getRootPath(){
		return gcsDirectoryManager.getRootPath();
	}

	@Override
	public boolean exists(PathbeanKey key, Config config){
		return gcsDirectoryManager.exists(key.getPathAndFile());
	}

	@Override
	public Optional<Long> length(PathbeanKey key, Config config){
		return gcsDirectoryManager.length(key.getPathAndFile());
	}

	@Override
	public Optional<byte[]> read(PathbeanKey key, Config config){
		return gcsDirectoryManager.read(key.getPathAndFile());
	}

	@Override
	public Optional<byte[]> readPartial(PathbeanKey key, long offset, int length, Config config){
		return gcsDirectoryManager.read(key.getPathAndFile(), offset, length);
	}

	@Override
	public Optional<byte[]> readEnding(PathbeanKey key, int length, Config config){
		return gcsDirectoryManager.readEnding(key.getPathAndFile(), length);
	}

	@Override
	public InputStream readInputStream(PathbeanKey key, Config config){
		return gcsDirectoryManager.readInputStream(key.getPathAndFile());
	}

	@Override
	public void write(PathbeanKey key, byte[] content, Config config){
		gcsDirectoryManager.write(key.getPathAndFile(), content);
	}

	//TODO implement multi-part uploads
	//TODO implement the writeParallel method
	@Override
	public void writeInputStream(PathbeanKey key, InputStream inputStream, Config config){
		gcsDirectoryManager.write(key.getPathAndFile(), inputStream);
	}

	@Override
	public void delete(PathbeanKey key, Config config){
		gcsDirectoryManager.delete(key.getPathAndFile());
	}

	@Override
	public void deleteMulti(List<PathbeanKey> keys, Config config){
		Scanner.of(keys)
				.map(PathbeanKey::getPathAndFile)
				.flush(gcsDirectoryManager::deleteMulti);
	}

	@Override
	public void deleteAll(Subpath subpath, Config config){
		gcsDirectoryManager.deleteAll(subpath);
	}

	@Override
	public Scanner<List<Pathbean>> scanPaged(Subpath subpath, Config config){
		return gcsDirectoryManager.scanGcsObjectsPaged(subpath)
				.map(blobs -> Scanner.of(blobs)
						.map(blob -> {
							String blobName = blob.getBlobId().getName();
							String relativePath = gcsDirectoryManager.relativePath(blobName);
							PathbeanKey key = PathbeanKey.of(relativePath);
							Long size = blob.getSize();
							return new Pathbean(key, size);
						})
						.list());
	}

	@Override
	public Scanner<List<PathbeanKey>> scanKeysPaged(Subpath subpath, Config config){
		return gcsDirectoryManager.scanKeysPaged(subpath)
				.map(page -> Scanner.of(page)
						.map(PathbeanKey::of)
						.list());
	}

	@Override
	public void vacuum(Config config){
		throw new UnsupportedOperationException();
	}

}
