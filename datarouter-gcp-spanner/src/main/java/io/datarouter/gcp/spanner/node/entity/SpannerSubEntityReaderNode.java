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
package io.datarouter.gcp.spanner.node.entity;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.datarouter.gcp.spanner.SpannerClientManager;
import io.datarouter.gcp.spanner.field.SpannerFieldCodecRegistry;
import io.datarouter.gcp.spanner.op.entity.read.SpannerEntityGetKeyOp;
import io.datarouter.gcp.spanner.op.entity.read.SpannerEntityGetOp;
import io.datarouter.gcp.spanner.scan.entity.SpannerEntityDatabeanScanner;
import io.datarouter.gcp.spanner.scan.entity.SpannerEntityKeyScanner;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.entity.EntityPartitioner;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.entity.SubEntitySortedMapStorageReaderNode;
import io.datarouter.storage.node.type.index.ManagedNodesHolder;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.serialize.fieldcache.EntityFieldInfo;
import io.datarouter.util.tuple.Range;

public class SpannerSubEntityReaderNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements SubEntitySortedMapStorageReaderNode<EK,PK,D,F>{

	protected final ManagedNodesHolder managedNodesHolder;
	protected final SpannerClientManager clientManager;
	protected final SpannerFieldCodecRegistry spannerFieldCodecRegistry;
	protected final EntityFieldInfo<EK,E> entityFieldInfo;
	protected final EntityPartitioner<EK> partitioner;

	public SpannerSubEntityReaderNode(
			NodeParams<PK,D,F> params,
			ClientType<?,?> clientType,
			EntityNodeParams<EK,E> entityNodeParams,
			ManagedNodesHolder managedNodesHolder,
			SpannerClientManager clientManager,
			SpannerFieldCodecRegistry spannerFieldCodecRegistry){
		super(params, clientType);
		this.managedNodesHolder = managedNodesHolder;
		this.clientManager = clientManager;
		this.spannerFieldCodecRegistry = spannerFieldCodecRegistry;
		this.entityFieldInfo = new EntityFieldInfo<>(entityNodeParams);
		this.partitioner = entityFieldInfo.getEntityPartitioner();
	}

	@Override
	public String getEntityNodePrefix(){
		return getFieldInfo().getEntityNodePrefix();
	}

	@Override
	public boolean exists(PK key, Config config){
		return !getKeys(Collections.singletonList(key), config).isEmpty();
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config){
		var op = new SpannerEntityGetKeyOp<>(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				keys,
				config,
				spannerFieldCodecRegistry,
				partitioner);
		return op.wrappedCall();
	}

	@Override
	public D get(PK key, Config config){
		return getMulti(Collections.singletonList(key), config).stream()
				.findFirst()
				.orElse(null);
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config){
		var op = new SpannerEntityGetOp<>(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				keys,
				config,
				spannerFieldCodecRegistry,
				partitioner);
		return op.wrappedCall();
	}

	@Override
	public Scanner<D> scan(Range<PK> range, Config config){
		return scanMulti(Collections.singletonList(range), config);
	}

	@Override
	public Scanner<D> scanMulti(Collection<Range<PK>> ranges, Config config){
		Integer offset = config.getOffset();
		Integer limit = config.getLimit();
		if(limit != null){
			config.setLimit(config.findOffset().orElse(0) + config.getLimit());
		}
		var scannner = partitioner.scanAllPartitions()
				.collate(partition -> new SpannerEntityDatabeanScanner<>(
								clientManager.getDatabaseClient(getClientId()),
								getFieldInfo(),
								ranges,
								config.setOffset(0),
								spannerFieldCodecRegistry,
								false,
								partition),
						(list1, list2) -> list1.get(0).compareTo(list2.get(0)))
				.collate(Scanner::of);
		if(offset != null){
			scannner = scannner.skip(offset);
		}
		if(limit != null){
			scannner = scannner.limit(limit);
		}
		return scannner;
	}

	@Override
	public Scanner<PK> scanKeysMulti(Collection<Range<PK>> ranges, Config config){
		Integer offset = config.getOffset();
		Integer limit = config.getLimit();
		if(limit != null){
			config.setLimit(config.findOffset().orElse(0) + config.getLimit());
		}
		var scannner = partitioner.scanAllPartitions()
				.collate(partition -> new SpannerEntityKeyScanner<>(
								clientManager.getDatabaseClient(getClientId()),
								getFieldInfo(),
								ranges,
								config.setOffset(0),
								spannerFieldCodecRegistry,
								false,
								partition),
						(list1, list2) -> list1.get(0).compareTo(list2.get(0)))
				.collate(Scanner::of);
		if(offset != null){
			scannner = scannner.skip(offset);
		}
		if(limit != null){
			scannner = scannner.limit(limit);
		}
		return scannner;
	}

}
