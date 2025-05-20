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
package io.datarouter.gcp.gcs.client;

import io.datarouter.gcp.gcs.GcsClientType;
import io.datarouter.gcp.gcs.node.GcsDirectoryManager;
import io.datarouter.gcp.gcs.node.GcsNode;
import io.datarouter.storage.client.imp.BlobClientNodeFactory;
import io.datarouter.storage.file.DatabaseBlob;
import io.datarouter.storage.file.DatabaseBlob.DatabaseBlobFielder;
import io.datarouter.storage.file.DatabaseBlobKey;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.adapter.NodeAdapters;
import io.datarouter.storage.node.op.raw.BlobStorage.PhysicalBlobStorageNode;
import io.datarouter.storage.util.Subpath;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class GcsClientNodeFactory
implements BlobClientNodeFactory{

	@Inject
	private GcsClientType gcsClientType;
	@Inject
	private GcsClientManager gcsClientManager;
	@Inject
	private NodeAdapters nodeAdapters;

	/*-------------- BlobClientNodeFactory --------------*/

	@Override
	public PhysicalBlobStorageNode createBlobNode(
			NodeParams<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> nodeParams){
		var node = createInternal(nodeParams);
		return nodeAdapters.wrapBlobNode(node);
	}

	/*-------------- private --------------*/

	private GcsNode createInternal(NodeParams<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> nodeParams){
		GcsClient client = gcsClientManager.getClient(nodeParams.getClientId());
		String bucket = nodeParams.getPhysicalName();
		Subpath path = nodeParams.getPathSupplier().get();
		var gcsDirectoryManager = new GcsDirectoryManager(client, bucket, path);
		return new GcsNode(nodeParams, gcsClientType, client, gcsDirectoryManager);
	}

}
