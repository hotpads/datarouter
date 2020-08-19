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

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.file.Pathbean;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.ObjectStorage.PhysicalObjectStorageNode;
import io.datarouter.storage.node.op.raw.write.ObjectStorageWriter;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;

public class S3Node<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements PhysicalObjectStorageNode<PK,D,F>, ObjectStorageWriter<PK,D>{

	private final S3DirectoryManager directoryManager;

	public S3Node(NodeParams<PK,D,F> params, ClientType<?,?> clientType, S3DirectoryManager directoryManager){
		super(params, clientType);
		this.directoryManager = directoryManager;
	}

	@Override
	public String getBucket(){
		return directoryManager.getBucket();
	}

	@Override
	public String getRootPath(){
		return directoryManager.getRootPath();
	}

	@Override
	public byte[] read(PathbeanKey key){
		return directoryManager.read(key.getPathAndFile());
	}

	@Override
	public void write(PathbeanKey key, byte[] content){
		directoryManager.write(key.getPathAndFile(), content);
	}

	@Override
	public String readUtf8(PathbeanKey key){
		return directoryManager.readUtf8(key.getPathAndFile());
	}

	@Override
	public void delete(PathbeanKey key){
		directoryManager.delete(key.getPathAndFile());
	}

	@Override
	public boolean exists(PathbeanKey key){
		return directoryManager.exists(key.getPathAndFile());
	}

	@Override
	public Scanner<Pathbean> scan(){
		return directoryManager.scanS3Objects()
				.map(s3Object -> {
					PathbeanKey key = PathbeanKey.of(directoryManager.relativePath(s3Object.key()));
					Long size = s3Object.size();
					return new Pathbean(key, size);
				});
	}

	@Override
	public Scanner<PathbeanKey> scanKeys(){
		return directoryManager.scanKeys()
				.map(PathbeanKey::of);
	}

}