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
package io.datarouter.client.mysql.node;

import java.util.Collection;
import java.util.List;

import io.datarouter.client.mysql.MysqlClientType;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.unique.UniqueKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.combo.reader.IndexedSortedMapStorageReader.PhysicalIndexedSortedMapStorageReaderNode;
import io.datarouter.storage.node.type.index.ManagedNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.serialize.fieldcache.IndexEntryFieldInfo;
import io.datarouter.util.tuple.Range;

public class MysqlReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements PhysicalIndexedSortedMapStorageReaderNode<PK,D,F>{

	public static final int DEFAULT_ITERATE_BATCH_SIZE = 1000;

	private final MysqlNodeManager mysqlNodeManager;

	public MysqlReaderNode(NodeParams<PK,D,F> params, MysqlClientType mysqlClientType,
			MysqlNodeManager mysqlNodeManager){
		super(params, mysqlClientType);
		this.mysqlNodeManager = mysqlNodeManager;
	}

	/*------------------------- MapStorageReader methods --------------------*/

	@Override
	public boolean exists(PK key, Config config){
		return mysqlNodeManager.exists(getFieldInfo(), key, config);
	}

	@Override
	public D get(final PK key, final Config config){
		return mysqlNodeManager.get(getFieldInfo(), key, config);
	}

	@Override
	public List<D> getMulti(final Collection<PK> keys, final Config config){
		return mysqlNodeManager.getMulti(getFieldInfo(), keys, config);
	}

	@Override
	public List<PK> getKeys(final Collection<PK> keys, final Config config){
		return mysqlNodeManager.getKeys(getFieldInfo(), keys, config);
	}


	/*------------------------ IndexedStorageReader methods -----------------*/

	@Override
	public D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
		return mysqlNodeManager.lookupUnique(getFieldInfo(), uniqueKey, config);
	}

	@Override
	public List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		return mysqlNodeManager.lookupMultiUnique(getFieldInfo(), uniqueKeys, config);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<IE> scanMultiIndex(IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo, Collection<Range<IK>> ranges,
			Config config){
		return mysqlNodeManager.scanMultiIndex(getFieldInfo(), indexEntryFieldInfo, ranges, config);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<D> scanMultiByIndex(IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo, Collection<Range<IK>> ranges,
			Config config){
		return mysqlNodeManager.scanMultiByIndex(getFieldInfo(), indexEntryFieldInfo, ranges, config);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<IK> scanMultiIndexKeys(IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo, Collection<Range<IK>> ranges,
			Config config){
		return mysqlNodeManager.scanMultiIndexKeys(getFieldInfo(), indexEntryFieldInfo, ranges, config);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<IE> getMultiFromIndex(Collection<IK> keys, Config config, IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		return mysqlNodeManager.getMultiFromIndex(getFieldInfo(), keys, config, indexEntryFieldInfo);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<D> getMultiByIndex(Collection<IK> keys, Config config, IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		return mysqlNodeManager.getMultiByIndex(getFieldInfo(), keys, config, indexEntryFieldInfo);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>,
			N extends ManagedNode<PK,D,IK,IE,IF>>
	N registerManaged(N managedNode){
		return mysqlNodeManager.registerManaged(getFieldInfo(), managedNode);
	}

	@Override
	public List<ManagedNode<PK,D,?,?,?>> getManagedNodes(){
		return mysqlNodeManager.getManagedNodes(getFieldInfo());
	}

	/*--------------------- SortedStorageReader methods ---------------------*/

	@Override
	public Scanner<PK> scanKeysMulti(Collection<Range<PK>> ranges, Config config){
		return mysqlNodeManager.scanKeysMulti(getFieldInfo(), ranges, config);
	}

	@Override
	public Scanner<D> scanMulti(Collection<Range<PK>> ranges, Config config){
		return mysqlNodeManager.scanMulti(getFieldInfo(), ranges, config);
	}

}
