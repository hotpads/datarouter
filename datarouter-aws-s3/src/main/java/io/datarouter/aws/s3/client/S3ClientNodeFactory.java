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
package io.datarouter.aws.s3.client;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.aws.s3.DatarouterS3Client;
import io.datarouter.aws.s3.S3ClientType;
import io.datarouter.aws.s3.node.S3DirectoryManager;
import io.datarouter.aws.s3.node.S3Node;
import io.datarouter.storage.client.imp.BlobClientNodeFactory;
import io.datarouter.storage.file.Pathbean;
import io.datarouter.storage.file.Pathbean.PathbeanFielder;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.adapter.NodeAdapters;
import io.datarouter.storage.node.op.raw.BlobStorage.PhysicalBlobStorageNode;
import io.datarouter.storage.util.Subpath;

@Singleton
public class S3ClientNodeFactory
implements BlobClientNodeFactory{

	@Inject
	private S3ClientType s3ClientType;
	@Inject
	private S3ClientManager s3ClientManager;
	@Inject
	private NodeAdapters nodeAdapters;

	/*---------------- BlobClientNodeFactory ------------------*/

	@Override
	public PhysicalBlobStorageNode createBlobNode(NodeParams<PathbeanKey,Pathbean,PathbeanFielder> nodeParams){
		var node = createInternal(nodeParams);
		return nodeAdapters.wrapBlobNode(node);
	}

	/*---------------- private ------------------*/

	private S3Node createInternal(NodeParams<PathbeanKey,Pathbean,PathbeanFielder> nodeParams){
		DatarouterS3Client client = s3ClientManager.getClient(nodeParams.getClientId());
		String bucket = nodeParams.getPhysicalName();
		Subpath path = nodeParams.getPath();
		var s3DirectoryManager = new S3DirectoryManager(client, bucket, path);
		return new S3Node(nodeParams, s3ClientType, client, s3DirectoryManager);
	}

}
