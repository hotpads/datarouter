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
import java.util.LinkedList;
import java.util.List;

import io.datarouter.client.mysql.execution.SessionExecutor;
import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.read.MysqlGetKeysOp;
import io.datarouter.client.mysql.op.read.MysqlGetOp;
import io.datarouter.client.mysql.op.read.MysqlGetOpExecutor;
import io.datarouter.client.mysql.op.read.MysqlGetPrimaryKeyRangesOp;
import io.datarouter.client.mysql.op.read.MysqlGetRangesOp;
import io.datarouter.client.mysql.op.read.MysqlLookupUniqueOp;
import io.datarouter.client.mysql.op.read.index.MysqlGetByIndexOp;
import io.datarouter.client.mysql.op.read.index.MysqlGetIndexOp;
import io.datarouter.client.mysql.op.read.index.MysqlManagedIndexGetDatabeanRangesOp;
import io.datarouter.client.mysql.op.read.index.MysqlManagedIndexGetKeyRangesOp;
import io.datarouter.client.mysql.op.read.index.MysqlManagedIndexGetRangesOp;
import io.datarouter.client.mysql.util.MysqlPreparedStatementBuilder;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.unique.UniqueKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.read.IndexedStorageReader;
import io.datarouter.storage.node.op.raw.read.MapStorageReader;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader;
import io.datarouter.storage.serialize.fieldcache.DatabeanFieldInfo;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.tuple.Range;

public class MysqlReaderOps<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	public static final int DEFAULT_ITERATE_BATCH_SIZE = 1000;

	private final MysqlReaderNode<PK,D,F> node;
	private final MysqlFieldCodecFactory fieldCodecFactory;
	private final Datarouter datarouter;
	private final MysqlGetOpExecutor mysqlGetOpExecutor;
	private final MysqlPreparedStatementBuilder mysqlPreparedStatementBuilder;

	public MysqlReaderOps(MysqlReaderNode<PK,D,F> node, MysqlFieldCodecFactory fieldCodecFactory, Datarouter datarouter,
			MysqlGetOpExecutor mysqlGetOpExecutor, MysqlPreparedStatementBuilder mysqlPreparedStatementBuilder){
		this.node = node;
		this.fieldCodecFactory = fieldCodecFactory;
		this.datarouter = datarouter;
		this.mysqlGetOpExecutor = mysqlGetOpExecutor;
		this.mysqlPreparedStatementBuilder = mysqlPreparedStatementBuilder;
	}

	/*------------------------- MapStorageReader methods --------------------*/

	public List<D> getMulti(Collection<PK> keys, Config config){
		String opName = MapStorageReader.OP_getMulti;
		MysqlGetOp<PK,D,F> op = new MysqlGetOp<>(datarouter, fieldCodecFactory, mysqlGetOpExecutor, node, opName, keys,
				Config.nullSafe(config));
		return new SessionExecutor<>(datarouter.getClientPool(), op, getTraceName(opName)).call();
	}

	public List<PK> getKeys(Collection<PK> keys, Config config){
		String opName = MapStorageReader.OP_getKeys;
		MysqlGetKeysOp<PK,D,F> op = new MysqlGetKeysOp<>(datarouter, fieldCodecFactory, mysqlGetOpExecutor, node,
				opName, keys, Config.nullSafe(config));
		return new SessionExecutor<>(datarouter.getClientPool(), op, getTraceName(opName)).call();
	}

	/*------------------------ IndexedStorageReader methods -----------------*/

	public D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
		String opName = IndexedStorageReader.OP_lookupUnique;
		MysqlLookupUniqueOp<PK,D,F> op = new MysqlLookupUniqueOp<>(datarouter, fieldCodecFactory, mysqlGetOpExecutor,
				node, opName, ListTool.wrap(uniqueKey), Config.nullSafe(config));
		List<D> result = new SessionExecutor<>(datarouter.getClientPool(), op, getTraceName(opName)).call();
		if(CollectionTool.size(result) > 1){
			throw new DataAccessException("found >1 databeans with unique index key=" + uniqueKey);
		}
		return CollectionTool.getFirst(result);
	}

	public List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		String opName = IndexedStorageReader.OP_lookupMultiUnique;
		if(CollectionTool.isEmpty(uniqueKeys)){
			return new LinkedList<>();
		}
		MysqlLookupUniqueOp<PK,D,F> op = new MysqlLookupUniqueOp<>(datarouter, fieldCodecFactory, mysqlGetOpExecutor,
				node, opName, uniqueKeys, Config.nullSafe(config));
		return new SessionExecutor<>(datarouter.getClientPool(), op, getTraceName(opName)).call();
	}

	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<IE> getMultiFromIndex(Collection<IK> keys, Config config, DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		String opName = IndexedStorageReader.OP_getFromIndex;
		BaseMysqlOp<List<IE>> op = new MysqlGetIndexOp<>(datarouter, mysqlGetOpExecutor, node, fieldCodecFactory,
				opName, config, indexEntryFieldInfo.getDatabeanSupplier(), indexEntryFieldInfo.getFielderSupplier(),
				keys);
		return new SessionExecutor<>(datarouter.getClientPool(), op, getTraceName(opName)).call();
	}

	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<D> getMultiByIndex(Collection<IK> keys, Config config, DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		String opName = IndexedStorageReader.OP_getByIndex;
		BaseMysqlOp<List<D>> op = new MysqlGetByIndexOp<>(datarouter, node, fieldCodecFactory,
				mysqlPreparedStatementBuilder, indexEntryFieldInfo, keys, config);
		return new SessionExecutor<>(datarouter.getClientPool(), op, getTraceName(opName)).call();
	}

	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<IE> getIndexRanges(Collection<Range<IK>> ranges, Config config,
			DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		String opName = IndexedStorageReader.OP_getIndexRange;
		MysqlManagedIndexGetRangesOp<PK,D,F,IK,IE,IF> op = new MysqlManagedIndexGetRangesOp<>(datarouter, node,
				fieldCodecFactory, mysqlPreparedStatementBuilder, indexEntryFieldInfo, ranges, config);
		return new SessionExecutor<>(datarouter.getClientPool(), op, getTraceName(opName)).call();
	}

	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<IK> getIndexKeyRanges(Collection<Range<IK>> ranges, Config config,
			DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		String opName = IndexedStorageReader.OP_getIndexKeyRange;
		MysqlManagedIndexGetKeyRangesOp<PK,D,F,IK,IE,IF> op = new MysqlManagedIndexGetKeyRangesOp<>(datarouter, node,
				fieldCodecFactory, mysqlPreparedStatementBuilder, indexEntryFieldInfo, ranges, config);
		return new SessionExecutor<>(datarouter.getClientPool(), op, getTraceName(opName)).call();
	}

	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<D> getIndexDatabeanRanges(Collection<Range<IK>> ranges, Config config,
			DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		String opName = IndexedStorageReader.OP_getIndexKeyRange;
		MysqlManagedIndexGetDatabeanRangesOp<PK,D,F,IK,IE,IF> op = new MysqlManagedIndexGetDatabeanRangesOp<>(
				datarouter, node, fieldCodecFactory, mysqlPreparedStatementBuilder, indexEntryFieldInfo, ranges,
				config);
		return new SessionExecutor<>(datarouter.getClientPool(), op, getTraceName(opName)).call();
	}

	/*--------------------- SortedStorageReader methods ---------------------*/

	public List<PK> getKeysInRanges(Collection<Range<PK>> ranges, Config config){
		if(ranges.stream().allMatch(Range::isEmpty)){
			return Collections.emptyList();
		}
		String opName = SortedStorageReader.OP_getKeysInRange;
		MysqlGetPrimaryKeyRangesOp<PK,D,F> op = new MysqlGetPrimaryKeyRangesOp<>(datarouter, node, fieldCodecFactory,
				mysqlPreparedStatementBuilder, ranges, config);
		return new SessionExecutor<>(datarouter.getClientPool(), op, getTraceName(opName)).call();
	}

	public List<D> getRanges(Collection<Range<PK>> ranges, Config config){
		if(ranges.stream().allMatch(Range::isEmpty)){
			return Collections.emptyList();
		}
		String opName = SortedStorageReader.OP_getRange;
		MysqlGetRangesOp<PK,D,F> op = new MysqlGetRangesOp<>(datarouter, node, fieldCodecFactory,
				mysqlPreparedStatementBuilder, ranges, config);
		return new SessionExecutor<>(datarouter.getClientPool(), op, getTraceName(opName)).call();
	}

	/*--------------------------------- helper ------------------------------*/

	public String getTraceName(String opName){
		return node.getName() + " " + opName;
	}
}
