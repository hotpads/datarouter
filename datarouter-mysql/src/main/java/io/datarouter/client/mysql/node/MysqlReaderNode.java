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
import java.util.Collections;
import java.util.List;

import io.datarouter.client.mysql.MysqlClient;
import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.client.mysql.op.read.MysqlGetOpExecutor;
import io.datarouter.client.mysql.scan.MysqlDatabeanScanner;
import io.datarouter.client.mysql.scan.MysqlManagedIndexDatabeanScanner;
import io.datarouter.client.mysql.scan.MysqlManagedIndexKeyScanner;
import io.datarouter.client.mysql.scan.MysqlManagedIndexScanner;
import io.datarouter.client.mysql.scan.MysqlPrimaryKeyScanner;
import io.datarouter.client.mysql.util.MysqlPreparedStatementBuilder;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.unique.UniqueKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.client.LazyClientProvider;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.combo.reader.IndexedSortedMapStorageReader.PhysicalIndexedSortedMapStorageReaderNode;
import io.datarouter.storage.node.type.index.ManagedNode;
import io.datarouter.storage.node.type.index.ManagedNodesHolder;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.serialize.fieldcache.DatabeanFieldInfo;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.iterable.scanner.Scanner;
import io.datarouter.util.iterable.scanner.iterable.SingleUseScannerIterable;
import io.datarouter.util.tuple.Range;

public class MysqlReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements PhysicalIndexedSortedMapStorageReaderNode<PK,D,F>{

	public static final int DEFAULT_ITERATE_BATCH_SIZE = 1000;

	private final MysqlReaderOps<PK,D,F> mysqlReaderOps;
	private final ManagedNodesHolder<PK,D> managedNodesHolder;
	private final DatarouterClients datarouterClients;
	private final DatarouterNodes datarouterNodes;

	public MysqlReaderNode(NodeParams<PK,D,F> params, MysqlFieldCodecFactory fieldCodecFactory, Datarouter datarouter,
			DatarouterClients datarouterClients, DatarouterNodes datarouterNodes, MysqlGetOpExecutor mysqlGetOpExecutor,
			MysqlPreparedStatementBuilder mysqlPreparedStatementBuilder){
		super(params);
		this.datarouterClients = datarouterClients;
		this.datarouterNodes = datarouterNodes;
		this.mysqlReaderOps = new MysqlReaderOps<>(this, fieldCodecFactory, datarouter, mysqlGetOpExecutor,
				mysqlPreparedStatementBuilder);
		this.managedNodesHolder = new ManagedNodesHolder<>();
	}

	@Override
	public MysqlClient getClient(){
		return (MysqlClient)datarouterClients.getClient(getFieldInfo().getClientId().getName());
	}

	/*------------------------- MapStorageReader methods --------------------*/


	@Override
	public boolean exists(PK key, Config config){
		return CollectionTool.notEmpty(mysqlReaderOps.getKeys(Collections.singleton(key), Config.nullSafe(config)));
	}

	@Override
	public D get(final PK key, final Config config){
		return CollectionTool.getFirst(mysqlReaderOps.getMulti(ListTool.wrap(key), Config.nullSafe(config)));
	}

	@Override
	public List<D> getMulti(final Collection<PK> keys, final Config config){
		return mysqlReaderOps.getMulti(keys, Config.nullSafe(config));
	}

	@Override
	public List<PK> getKeys(final Collection<PK> keys, final Config config){
		return mysqlReaderOps.getKeys(keys, config);
	}


	/*------------------------ IndexedStorageReader methods -----------------*/

	@Override
	public D lookupUnique(final UniqueKey<PK> uniqueKey, final Config config){
		return mysqlReaderOps.lookupUnique(uniqueKey, config);
	}

	@Override
	public List<D> lookupMultiUnique(final Collection<? extends UniqueKey<PK>> uniqueKeys, final Config config){
		return mysqlReaderOps.lookupMultiUnique(uniqueKeys, config);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Iterable<IE> scanMultiIndex(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Collection<Range<IK>> ranges,
			Config config){
		return new SingleUseScannerIterable<>(new MysqlManagedIndexScanner<>(mysqlReaderOps, indexEntryFieldInfo,
				ranges, config));
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Iterable<D> scanMultiByIndex(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Collection<Range<IK>> ranges,
			Config config){
		return new SingleUseScannerIterable<>(new MysqlManagedIndexDatabeanScanner<>(mysqlReaderOps,
				indexEntryFieldInfo, ranges, config));
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Iterable<IK> scanMultiIndexKeys(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Collection<Range<IK>> ranges,
			Config config){
		return new SingleUseScannerIterable<>(new MysqlManagedIndexKeyScanner<>(mysqlReaderOps, indexEntryFieldInfo,
				ranges, config));
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<IE> getMultiFromIndex(Collection<IK> keys, Config config, DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		return mysqlReaderOps.getMultiFromIndex(keys, config, indexEntryFieldInfo);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<D> getMultiByIndex(Collection<IK> keys, Config config, DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		return mysqlReaderOps.getMultiByIndex(keys, config, indexEntryFieldInfo);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>,
			N extends ManagedNode<PK,D,IK,IE,IF>>
	N registerManaged(N managedNode){
		boolean isRegistered = datarouterNodes.getAllNodes().contains(this);
		LazyClientProvider lazyClientProvider = datarouterClients.getLazyClientProviderByName()
				.get(getFieldInfo().getClientId().getName());
		boolean isClientInitialized = lazyClientProvider != null && lazyClientProvider.isInitialized();
		if(isRegistered && isClientInitialized){
			throw new RuntimeException(this + " is already registered and initialized, can't register index "
					+ managedNode);
		}
		return managedNodesHolder.registerManagedNode(managedNode);
	}

	@Override
	public List<ManagedNode<PK,D,?,?,?>> getManagedNodes(){
		return managedNodesHolder.getManagedNodes();
	}

	/*--------------------- SortedStorageReader methods ---------------------*/

	@Override
	public Iterable<PK> scanKeysMulti(Collection<Range<PK>> ranges, Config config){
		Scanner<PK> scanner = new MysqlPrimaryKeyScanner<>(mysqlReaderOps, ranges, config);
		return new SingleUseScannerIterable<>(scanner);
	}

	@Override
	public Iterable<D> scanMulti(Collection<Range<PK>> ranges, Config config){
		Scanner<D> scanner = new MysqlDatabeanScanner<>(mysqlReaderOps, ranges, config);
		return new SingleUseScannerIterable<>(scanner);
	}


	/*--------------------------------- helper ------------------------------*/

	public String getTraceName(String opName){
		return mysqlReaderOps.getTraceName(opName);
	}

}
