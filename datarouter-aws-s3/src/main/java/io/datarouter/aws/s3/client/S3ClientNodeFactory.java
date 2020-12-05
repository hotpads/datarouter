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
package io.datarouter.aws.s3.client;

import java.util.List;
import java.util.function.UnaryOperator;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.aws.s3.DatarouterS3Client;
import io.datarouter.aws.s3.S3ClientType;
import io.datarouter.aws.s3.node.S3DirectoryManager;
import io.datarouter.aws.s3.node.S3Node;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.imp.BaseClientNodeFactory;
import io.datarouter.storage.client.imp.BlobClientNodeFactory;
import io.datarouter.storage.client.imp.WrappedNodeFactory;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.op.raw.BlobStorage.PhysicalBlobStorageNode;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.util.Subpath;

@Singleton
public class S3ClientNodeFactory extends BaseClientNodeFactory implements BlobClientNodeFactory{

	@Inject
	private S3ClientType s3ClientType;
	@Inject
	private S3ClientManager s3ClientManager;

	public class S3WrappedNodeFactory<
			EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends WrappedNodeFactory<EK,E,PK,D,F,PhysicalBlobStorageNode<PK,D,F>>{

		@Override
		public PhysicalBlobStorageNode<PK,D,F> createNode(
				EntityNodeParams<EK,E> entityNodeParams,
				NodeParams<PK,D,F> nodeParams){
			return createInternal(nodeParams);
		}

		@Override
		public List<UnaryOperator<PhysicalBlobStorageNode<PK,D,F>>> getAdapters(){
			return List.of();
		}

	}

	@Override
	public <EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	WrappedNodeFactory<EK,E,PK,D,F,?> makeWrappedNodeFactory(){
		return new S3WrappedNodeFactory<>();
	}

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D,F> createBlobNode(NodeParams<PK,D,F> nodeParams){
		return createInternal(nodeParams);
	}

	private <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	S3Node<PK,D,F> createInternal(NodeParams<PK,D,F> nodeParams){
		DatarouterS3Client client = s3ClientManager.getClient(nodeParams.getClientId());
		String bucket = nodeParams.getPhysicalName();
		Subpath path = nodeParams.getPath();
		var s3DirectoryManager = new S3DirectoryManager(client, bucket, path);
		return new S3Node<>(nodeParams, s3ClientType, client, s3DirectoryManager);
	}

}
