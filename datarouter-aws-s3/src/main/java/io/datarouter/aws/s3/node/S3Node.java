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

import io.datarouter.aws.s3.DatarouterS3Client;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.file.Pathbean;
import io.datarouter.storage.file.Pathbean.PathbeanFielder;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.ObjectStorage.PhysicalObjectStorageNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.util.Subpath;

public class S3Node<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements PhysicalObjectStorageNode<PK,D,F>{

	private final DatarouterS3Client datarouterS3Client;
	private final S3DirectoryManager s3DirectoryManager;

	public S3Node(
			NodeParams<PK,D,F> params,
			ClientType<?,?> clientType,
			DatarouterS3Client datarouterS3Client,
			S3DirectoryManager directoryManager){
		super(params, clientType);
		this.datarouterS3Client = datarouterS3Client;
		this.s3DirectoryManager = directoryManager;
	}

	public static S3Node<PathbeanKey,Pathbean,PathbeanFielder> cast(
			ObjectStorageNode<PathbeanKey,Pathbean,PathbeanFielder> objectStorageNode){
		return (S3Node<PathbeanKey,Pathbean,PathbeanFielder>)objectStorageNode;
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
	public byte[] read(PathbeanKey key){
		return s3DirectoryManager.read(key.getPathAndFile());
	}

	@Override
	public void write(PathbeanKey key, byte[] content){
		s3DirectoryManager.write(key.getPathAndFile(), content);
	}

	@Override
	public String readUtf8(PathbeanKey key){
		return s3DirectoryManager.readUtf8(key.getPathAndFile());
	}

	@Override
	public void delete(PathbeanKey key){
		s3DirectoryManager.delete(key.getPathAndFile());
	}

	@Override
	public boolean exists(PathbeanKey key){
		return s3DirectoryManager.exists(key.getPathAndFile());
	}

	@Override
	public Scanner<Pathbean> scan(Subpath subpath){
		return s3DirectoryManager.scanS3Objects(subpath)
				.map(s3Object -> {
					PathbeanKey key = PathbeanKey.of(s3DirectoryManager.relativePath(s3Object.key()));
					Long size = s3Object.size();
					return new Pathbean(key, size);
				});
	}

	@Override
	public Scanner<PathbeanKey> scanKeys(Subpath subpath){
		return s3DirectoryManager.scanKeys(subpath)
				.map(PathbeanKey::of);
	}

}
