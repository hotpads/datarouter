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
package io.datarouter.storage.node.builder;

import java.util.function.Supplier;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.file.Pathbean;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.factory.BlobNodeFactory;
import io.datarouter.storage.node.op.NodeOps;
import io.datarouter.storage.node.op.raw.BlobStorage.PhysicalBlobStorageNode;
import io.datarouter.storage.util.Subpath;

public class BlobNodeBuilder<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	protected final Datarouter datarouter;
	protected final BlobNodeFactory blobNodeFactory;
	protected final ClientId clientId;
	protected final Supplier<D> databeanSupplier;
	protected final Supplier<F> fielderSupplier;

	protected String bucket;
	protected Subpath path;

	public BlobNodeBuilder(
			Datarouter datarouter,
			BlobNodeFactory blobNodeFactory,
			ClientId clientId,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
		this.datarouter = datarouter;
		this.blobNodeFactory = blobNodeFactory;
		this.clientId = clientId;
		this.databeanSupplier = databeanSupplier;
		this.fielderSupplier = fielderSupplier;
	}

	public <N extends NodeOps<PathbeanKey,Pathbean>> N build(){
		return blobNodeFactory.create(clientId, bucket, path);
	}

	public PhysicalBlobStorageNode buildAndRegister(){
		return datarouter.register(blobNodeFactory.create(clientId, bucket, path));
	}

}