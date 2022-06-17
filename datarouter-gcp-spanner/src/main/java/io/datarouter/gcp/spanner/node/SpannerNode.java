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
package io.datarouter.gcp.spanner.node;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.datarouter.gcp.spanner.SpannerClientManager;
import io.datarouter.gcp.spanner.SpannerClientType;
import io.datarouter.gcp.spanner.field.SpannerFieldCodecs;
import io.datarouter.gcp.spanner.op.read.index.SpannerGetByIndexOp;
import io.datarouter.gcp.spanner.op.read.index.SpannerGetFromIndexOp;
import io.datarouter.gcp.spanner.op.read.index.SpannerLookupUniqueOp;
import io.datarouter.gcp.spanner.op.read.index.write.SpannerDeleteByIndexOp;
import io.datarouter.gcp.spanner.op.read.index.write.SpannerDeleteUniqueOp;
import io.datarouter.gcp.spanner.op.write.SpannerDeleteAllOp;
import io.datarouter.gcp.spanner.op.write.SpannerDeleteOp;
import io.datarouter.gcp.spanner.op.write.SpannerPutOp;
import io.datarouter.gcp.spanner.scan.SpannerByIndexKeyScanner;
import io.datarouter.gcp.spanner.scan.SpannerByIndexScanner;
import io.datarouter.gcp.spanner.scan.SpannerFromIndexScanner;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.unique.UniqueKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage.PhysicalIndexedSortedMapStorageNode;
import io.datarouter.storage.node.type.index.ManagedNode;
import io.datarouter.storage.node.type.index.ManagedNodesHolder;
import io.datarouter.storage.serialize.fieldcache.IndexEntryFieldInfo;
import io.datarouter.util.tuple.Range;

public class SpannerNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SpannerReaderNode<PK,D,F>
implements PhysicalIndexedSortedMapStorageNode<PK,D,F>{

	public SpannerNode(
			NodeParams<PK,D,F> params,
			SpannerClientType clientType,
			ManagedNodesHolder managedNodesHolder,
			SpannerClientManager clientManager,
			SpannerFieldCodecs fieldCodecs){
		super(params, clientType, managedNodesHolder, clientManager, fieldCodecs);
	}

	@Override
	public D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
		return lookupMultiUnique(Collections.singletonList(uniqueKey), config).stream().findFirst().orElse(null);
	}

	@Override
	public List<D> lookupMultiUnique(
			Collection<? extends UniqueKey<PK>> uniqueKeys,
			Config config){
		var spannerLookupUniqueOp = new SpannerLookupUniqueOp<>(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				uniqueKeys,
				config,
				fieldCodecs);
		return spannerLookupUniqueOp.wrappedCall();
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<IE> getMultiFromIndex(
			Collection<IK> keys,
			Config config,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		var getFromIndexOp = new SpannerGetFromIndexOp<>(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				keys,
				config,
				fieldCodecs,
				indexEntryFieldInfo);
		return getFromIndexOp.wrappedCall();
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<D> getMultiByIndex(
			Collection<IK> keys,
			Config config,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		var spannerGetByIndexOp = new SpannerGetByIndexOp<>(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				keys,
				config,
				fieldCodecs,
				indexEntryFieldInfo.getIndexName());
		return spannerGetByIndexOp.wrappedCall();
	}

	@SuppressWarnings("resource")
	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<IE> scanRangesIndex(
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo,
			Collection<Range<IK>> ranges,
			Config config){
		return new SpannerFromIndexScanner<>(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				ranges,
				config,
				fieldCodecs,
				indexEntryFieldInfo,
				false)
				.concat(Scanner::of);
	}

	@SuppressWarnings("resource")
	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<D> scanRangesByIndex(
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo,
			Collection<Range<IK>> ranges,
			Config config){
		return new SpannerByIndexScanner<>(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				ranges,
				config,
				fieldCodecs,
				indexEntryFieldInfo,
				false)
				.concat(Scanner::of);
	}

	@SuppressWarnings("resource")
	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<IK> scanRangesIndexKeys(
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo,
			Collection<Range<IK>> ranges,
			Config config){
		return new SpannerByIndexKeyScanner<>(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				ranges,
				config,
				fieldCodecs,
				indexEntryFieldInfo,
				false)
				.concat(Scanner::of);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>,
			N extends ManagedNode<PK,D,IK,IE,IF>>
	N registerManaged(N managedNode){
		return managedNodesHolder.registerManagedNode(getFieldInfo(), managedNode);
	}

	@Override
	public List<ManagedNode<PK,D,?,?,?>> getManagedNodes(){
		return managedNodesHolder.getManagedNodes(getFieldInfo());
	}

	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		deleteMultiUnique(Collections.singletonList(uniqueKey), config);
	}

	@Override
	public void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		var op = new SpannerDeleteUniqueOp<>(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				uniqueKeys,
				config,
				fieldCodecs);
		op.wrappedCall();
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	void deleteByIndex(
			Collection<IK> keys,
			Config config,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		var op = new SpannerDeleteByIndexOp<>(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				keys,
				config,
				fieldCodecs,
				indexEntryFieldInfo.getIndexName());
		op.wrappedCall();
	}

	@Override
	public void delete(PK key, Config config){
		deleteMulti(Collections.singletonList(key), config);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config){
		var op = new SpannerDeleteOp<>(
				clientManager.getDatabaseClient(getFieldInfo().getClientId()),
				getFieldInfo(),
				keys,
				config,
				fieldCodecs);
		op.wrappedCall();
	}

	@Override
	public void deleteAll(Config config){
		var op = new SpannerDeleteAllOp<>(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				config);
		op.wrappedCall();
	}

	@Override
	public void put(D databean, Config config){
		putMulti(Collections.singletonList(databean), config);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		var putOp = new SpannerPutOp<>(
				clientManager.getDatabaseClient(getFieldInfo().getClientId()),
				getFieldInfo(),
				databeans,
				config,
				fieldCodecs);
		putOp.wrappedCall();
	}

}
