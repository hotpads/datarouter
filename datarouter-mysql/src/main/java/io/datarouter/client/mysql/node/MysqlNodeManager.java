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
package io.datarouter.client.mysql.node;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.mysql.MysqlClientType;
import io.datarouter.client.mysql.ddl.domain.MysqlCollation;
import io.datarouter.client.mysql.execution.MysqlOpRetryTool;
import io.datarouter.client.mysql.execution.SessionExecutor;
import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
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
import io.datarouter.client.mysql.op.write.MysqlDeleteAllOp;
import io.datarouter.client.mysql.op.write.MysqlDeleteByIndexOp;
import io.datarouter.client.mysql.op.write.MysqlDeleteOp;
import io.datarouter.client.mysql.op.write.MysqlIncrementOp;
import io.datarouter.client.mysql.op.write.MysqlPutOp;
import io.datarouter.client.mysql.op.write.MysqlUniqueIndexDeleteOp;
import io.datarouter.client.mysql.scan.MysqlDatabeanScanner;
import io.datarouter.client.mysql.scan.MysqlManagedIndexDatabeanScanner;
import io.datarouter.client.mysql.scan.MysqlManagedIndexKeyScanner;
import io.datarouter.client.mysql.scan.MysqlManagedIndexScanner;
import io.datarouter.client.mysql.scan.MysqlPrimaryKeyScanner;
import io.datarouter.client.mysql.sql.MysqlSqlFactory;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.unique.UniqueKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.read.IndexedStorageReader;
import io.datarouter.storage.node.op.raw.read.MapStorageReader;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader;
import io.datarouter.storage.node.op.raw.write.IndexedStorageWriter;
import io.datarouter.storage.node.op.raw.write.MapStorageWriter;
import io.datarouter.storage.node.type.index.ManagedNode;
import io.datarouter.storage.node.type.index.ManagedNodesHolder;
import io.datarouter.storage.serialize.fieldcache.IndexEntryFieldInfo;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.storage.tally.Tally;
import io.datarouter.storage.tally.Tally.TallyFielder;
import io.datarouter.storage.tally.TallyKey;
import io.datarouter.util.tuple.Range;

@Singleton
public class MysqlNodeManager{

	@Inject
	private Datarouter datarouter;
	@Inject
	private MysqlFieldCodecFactory fieldCodecFactory;
	@Inject
	private MysqlGetOpExecutor mysqlGetOpExecutor;
	@Inject
	private MysqlSqlFactory mysqlSqlFactory;
	@Inject
	private MysqlClientType mysqlClientType;
	@Inject
	private ManagedNodesHolder managedNodesHolder;
	@Inject
	private SessionExecutor sessionExecutor;

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	boolean exists(
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			PK key,
			Config config){
		List<PK> result = getKeys(fieldInfo, Collections.singleton(key), config);
		return result != null && !result.isEmpty();
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	D get(
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			PK key,
			Config config){
		if(key == null){
			return null;
		}
		return getMulti(fieldInfo, List.of(key), config).stream().findFirst().orElse(null);
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	List<PK> getKeys(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, Collection<PK> keys, Config config){
		String opName = MapStorageReader.OP_getKeys;
		var op = new MysqlGetKeysOp<>(datarouter, fieldCodecFactory, mysqlGetOpExecutor, fieldInfo, opName, keys,
				config);
		return sessionExecutor.runWithoutRetries(op);
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	List<D> getMulti(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, Collection<PK> keys, Config config){
		String opName = MapStorageReader.OP_getMulti;
		var op = new MysqlGetOp<>(datarouter, fieldCodecFactory, mysqlGetOpExecutor, fieldInfo, opName, keys, config);
		return sessionExecutor.runWithoutRetries(op);
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	D lookupUnique(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, UniqueKey<PK> uniqueKey, Config config){
		if(uniqueKey == null){
			return null;
		}
		String opName = IndexedStorageReader.OP_lookupUnique;
		MysqlLookupUniqueOp<PK,D,F> op = new MysqlLookupUniqueOp<>(
				datarouter,
				fieldCodecFactory,
				mysqlGetOpExecutor,
				fieldInfo,
				opName,
				List.of(uniqueKey), config);
		List<D> result = sessionExecutor.runWithoutRetries(op, getTraceName(fieldInfo.getNodeName(), opName));
		if(result.size() > 1){
			throw new DataAccessException("found >1 databeans with unique index key=" + uniqueKey);
		}
		return result.stream().findFirst().orElse(null);
	}

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	List<D> lookupMultiUnique(
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<? extends UniqueKey<PK>> uniqueKeys,
			Config config){
		String opName = IndexedStorageReader.OP_lookupMultiUnique;
		if(uniqueKeys == null || uniqueKeys.isEmpty()){
			return new LinkedList<>();
		}
		var op = new MysqlLookupUniqueOp<>(datarouter, fieldCodecFactory, mysqlGetOpExecutor, fieldInfo, opName,
				uniqueKeys, config);
		return sessionExecutor.runWithoutRetries(op, getTraceName(fieldInfo.getNodeName(), opName));
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<IE> getMultiFromIndex(
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<IK> keys,
			Config config,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		String opName = IndexedStorageReader.OP_getFromIndex;
		var op = new MysqlGetIndexOp<>(datarouter, mysqlGetOpExecutor, fieldInfo, fieldCodecFactory, opName, config,
				indexEntryFieldInfo, keys);
		return sessionExecutor.runWithoutRetries(op);
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<D> getMultiByIndex(
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<IK> keys,
			Config config,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		String indexName = indexEntryFieldInfo.getIndexName();
		String opName = IndexedStorageReader.OP_getByIndex;
		var op = new MysqlGetByIndexOp<>(datarouter, fieldInfo, fieldCodecFactory, mysqlSqlFactory, mysqlClientType,
				indexEntryFieldInfo, keys, config);
		return sessionExecutor.runWithoutRetries(op, getTraceName(indexName, opName));
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<IE> getIndexRanges(
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<Range<IK>> ranges,
			Config config,
		IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		String indexName = indexEntryFieldInfo.getIndexName();
		String opName = IndexedStorageReader.OP_getIndexRange;
		var op = new MysqlManagedIndexGetRangesOp<>(datarouter, fieldInfo, fieldCodecFactory, mysqlSqlFactory,
				indexEntryFieldInfo, ranges, config, mysqlClientType);
		return sessionExecutor.runWithoutRetries(op, getTraceName(indexName, opName));
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<IK> getIndexKeyRanges(
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<Range<IK>> ranges,
			Config config,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		String indexName = indexEntryFieldInfo.getIndexName();
		String opName = IndexedStorageReader.OP_getIndexKeyRange;
		var op = new MysqlManagedIndexGetKeyRangesOp<>(datarouter, fieldInfo, fieldCodecFactory, mysqlSqlFactory,
				indexEntryFieldInfo, ranges, config, mysqlClientType);
		return sessionExecutor.runWithoutRetries(op, getTraceName(indexName, opName));
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<D> getIndexDatabeanRanges(
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<Range<IK>> ranges,
			Config config,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		String indexName = indexEntryFieldInfo.getIndexName();
		String opName = IndexedStorageReader.OP_getByIndexRange;
		var op = new MysqlManagedIndexGetDatabeanRangesOp<>(datarouter, fieldInfo, fieldCodecFactory, mysqlSqlFactory,
				indexEntryFieldInfo, ranges, config, mysqlClientType);
		return sessionExecutor.runWithoutRetries(op, getTraceName(indexName, opName));
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	List<PK> getKeysInRanges(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, Collection<Range<PK>> ranges, Config config){
		if(ranges.stream().allMatch(Range::isEmpty)){
			return List.of();
		}
		String opName = SortedStorageReader.OP_getKeysInRange;
		var op = new MysqlGetPrimaryKeyRangesOp<>(datarouter, fieldInfo, fieldCodecFactory, mysqlSqlFactory, ranges,
				config, mysqlClientType);
		return sessionExecutor.runWithoutRetries(op, getTraceName(fieldInfo.getNodeName(), opName));
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	List<D> getRanges(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, Collection<Range<PK>> ranges, Config config){
		if(ranges.stream().allMatch(Range::isEmpty)){
			return List.of();
		}
		String opName = SortedStorageReader.OP_getRange;
		var op = new MysqlGetRangesOp<>(datarouter, fieldInfo, fieldCodecFactory, mysqlSqlFactory, ranges, config,
				mysqlClientType);
		return sessionExecutor.runWithoutRetries(op, getTraceName(fieldInfo.getNodeName(), opName));
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	void put(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, D databean, Config config){
		if(databean == null){
			return;
		}
		var op = new MysqlPutOp<>(
				datarouter,
				fieldInfo,
				this,
				mysqlSqlFactory,
				List.of(databean),
				config);
		MysqlOpRetryTool.tryNTimes(sessionExecutor.makeCallable(op, null), config);
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	void putMulti(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, Collection<D> databeans, Config config){
		if(databeans == null || databeans.isEmpty()){
			return;// avoid starting txn
		}
		var op = new MysqlPutOp<>(datarouter, fieldInfo, this, mysqlSqlFactory, databeans, config);
		MysqlOpRetryTool.tryNTimes(sessionExecutor.makeCallable(op, null), config);
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	void deleteAll(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, Config config){
		var op = new MysqlDeleteAllOp<>(datarouter, fieldInfo, config, mysqlSqlFactory);
		MysqlOpRetryTool.tryNTimes(sessionExecutor.makeCallable(op, null), config);
	}

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	void delete(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, PK key, Config config){
		if(key == null){
			return;
		}
		String opName = MapStorageWriter.OP_delete;
		var op = new MysqlDeleteOp<>(
				datarouter,
				fieldInfo,
				mysqlSqlFactory,
				mysqlClientType,
				List.of(key),
				config,
				opName);
		MysqlOpRetryTool.tryNTimes(sessionExecutor.makeCallable(op, null), config);
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	void deleteMulti(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, Collection<PK> keys, Config config){
		if(keys == null || keys.isEmpty()){
			return;// avoid starting txn
		}
		String opName = MapStorageWriter.OP_deleteMulti;
		var op = new MysqlDeleteOp<>(datarouter, fieldInfo, mysqlSqlFactory, mysqlClientType, keys, config, opName);
		MysqlOpRetryTool.tryNTimes(sessionExecutor.makeCallable(op, null), config);
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	void deleteUnique(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, UniqueKey<PK> uniqueKey, Config config){
		if(uniqueKey == null){
			return;
		}
		String opName = IndexedStorageWriter.OP_deleteUnique;
		var op = new MysqlUniqueIndexDeleteOp<>(
				datarouter,
				fieldInfo,
				mysqlSqlFactory,
				mysqlClientType,
				List.of(uniqueKey),
				config,
				opName);
		MysqlOpRetryTool.tryNTimes(sessionExecutor.makeCallable(op, getTraceName(fieldInfo.getNodeName(), opName)),
				config);
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	void deleteMultiUnique(
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<? extends UniqueKey<PK>> uniqueKeys,
			Config config){
		if(uniqueKeys == null || uniqueKeys.isEmpty()){
			return;// avoid starting txn
		}
		String opName = IndexedStorageWriter.OP_deleteMultiUnique;
		var op = new MysqlUniqueIndexDeleteOp<>(datarouter, fieldInfo, mysqlSqlFactory, mysqlClientType, uniqueKeys,
				config, opName);
		MysqlOpRetryTool.tryNTimes(
				sessionExecutor.makeCallable(op, getTraceName(fieldInfo.getNodeName(), opName)),
				config);
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			IK extends PrimaryKey<IK>>
	void deleteByIndex(
			PhysicalDatabeanFieldInfo<PK,D,F> databeanfieldInfo,
			Collection<IK> keys,
			Config config,
			IndexEntryFieldInfo<IK,?,?> indexEntryFieldInfo){
		String indexName = indexEntryFieldInfo.getIndexName();
		String opName = IndexedStorageWriter.OP_deleteByIndex;
		var op = new MysqlDeleteByIndexOp<>(
				datarouter,
				databeanfieldInfo,
				mysqlSqlFactory,
				mysqlClientType,
				keys,
				config,
				indexName,
				opName);
		MysqlOpRetryTool.tryNTimes(
				sessionExecutor.makeCallable(op, getTraceName(indexName, opName)),
				config);
	}

	@SuppressWarnings("resource")
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<IE> scanRangesIndex(
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo,
			Collection<Range<IK>> ranges,
			Config config){
		return new MysqlManagedIndexScanner<>(
				this,
				indexEntryFieldInfo,
				fieldInfo,
				ranges,
				config,
				MysqlCollation.isCaseInsensitive(fieldInfo))
				.concat(Scanner::of);
	}

	@SuppressWarnings("resource")
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<D> scanRangesByIndex(
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo,
			Collection<Range<IK>> ranges,
			Config config){
		return new MysqlManagedIndexDatabeanScanner<>(
				this,
				fieldInfo,
				indexEntryFieldInfo,
				ranges,
				config,
				MysqlCollation.isCaseInsensitive(fieldInfo))
				.concat(Scanner::of);
	}

	@SuppressWarnings("resource")
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<IK> scanRangesIndexKeys(
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo,
			Collection<Range<IK>> ranges,
			Config config){
		return new MysqlManagedIndexKeyScanner<>(
				this,
				fieldInfo,
				indexEntryFieldInfo,
				ranges,
				config,
				MysqlCollation.isCaseInsensitive(fieldInfo))
				.concat(Scanner::of);
	}

	@SuppressWarnings("resource")
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	Scanner<PK> scanRangesKeys(
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<Range<PK>> ranges,
			Config config){
		return new MysqlPrimaryKeyScanner<>(
				this,
				fieldInfo,
				ranges,
				config,
				MysqlCollation.isCaseInsensitive(fieldInfo))
				.concat(Scanner::of);
	}

	@SuppressWarnings("resource")
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	Scanner<D> scanRanges(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, Collection<Range<PK>> ranges, Config config){
		return new MysqlDatabeanScanner<>(
				this,
				fieldInfo,
				ranges,
				config,
				MysqlCollation.isCaseInsensitive(fieldInfo))
				.concat(Scanner::of);
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>,
			N extends ManagedNode<PK,D,IK,IE,IF>>
	N registerManaged(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, N managedNode){
		return managedNodesHolder.registerManagedNode(fieldInfo, managedNode);
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	List<ManagedNode<PK,D,?,?,?>> getManagedNodes(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo){
		return managedNodesHolder.getManagedNodes(fieldInfo);
	}

	public Long increment(PhysicalDatabeanFieldInfo<TallyKey,Tally,TallyFielder> fieldInfo, String key, Long amount){
		var op = new MysqlIncrementOp<>(
				datarouter,
				fieldInfo,
				fieldCodecFactory,
				mysqlSqlFactory,
				key,
				amount);
		return sessionExecutor.runWithoutRetries(op);
	}

	private static String getTraceName(String nodeName, String opName){
		return nodeName + " " + opName;
	}

}
