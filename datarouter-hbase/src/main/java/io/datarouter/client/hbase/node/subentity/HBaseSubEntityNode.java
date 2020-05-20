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
package io.datarouter.client.hbase.node.subentity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.ColumnPrefixFilter;

import io.datarouter.client.hbase.HBaseClientManager;
import io.datarouter.client.hbase.callback.CountingBatchCallbackFactory;
import io.datarouter.client.hbase.callback.CountingBatchCallbackFactory.CountingBatchCallback;
import io.datarouter.client.hbase.config.DatarouterHBaseExecutors.DatarouterHbaseClientExecutor;
import io.datarouter.client.hbase.node.HBaseIncrement;
import io.datarouter.client.hbase.node.nonentity.HBaseNode;
import io.datarouter.client.hbase.util.HBaseConfigTool;
import io.datarouter.client.hbase.util.HBaseTableTool;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.instrumentation.trace.TracerTool.TraceSpanInfoBuilder;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.entity.EntityTool;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.comparable.SignedByteField;
import io.datarouter.model.field.imp.comparable.SignedByteFieldKey;
import io.datarouter.model.field.imp.positive.UInt63Field;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.entity.PhysicalSubEntitySortedMapStorageNode;
import io.datarouter.storage.node.op.raw.MapStorage;
import io.datarouter.storage.node.op.raw.write.StorageWriter;
import io.datarouter.storage.util.DatarouterCounters;
import io.datarouter.util.bytes.ByteTool;
import io.datarouter.util.bytes.StringByteTool;
import io.datarouter.util.lang.ObjectTool;

public class HBaseSubEntityNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends HBaseSubEntityReaderNode<EK,E,PK,D,F>
implements PhysicalSubEntitySortedMapStorageNode<EK,PK,D,F>, HBaseIncrement<PK>{

	public static final byte[] FAM = HBaseNode.FAM;
	public static final SignedByteFieldKey DUMMY = HBaseNode.DUMMY_FIELD_KEY;

	private static final int DEFAULT_WRITE_BATCH_SIZE = 100;

	private final CountingBatchCallback<?> putMultiCallback;
	private final CountingBatchCallback<?> deleteAllCallback;
	private final CountingBatchCallback<?> deleteMultiCallback;
	private final ClientType<?,?> clientType;

	public HBaseSubEntityNode(
			HBaseClientManager hBaseClientManager,
			CountingBatchCallbackFactory countingBatchCallbackFactory,
			EntityNodeParams<EK,E> entityNodeParams,
			NodeParams<PK,D,F> params,
			ClientType<?,?> clientType,
			DatarouterHbaseClientExecutor datarouterHbaseClientExecutor){
		super(hBaseClientManager, entityNodeParams, params, clientType, datarouterHbaseClientExecutor);
		this.clientType = clientType;
		// can't access "client" yet, so extract these strings from elsewhere
		this.putMultiCallback = countingBatchCallbackFactory.new CountingBatchCallback<>(this,
				StorageWriter.OP_putMulti);
		this.deleteAllCallback = countingBatchCallbackFactory.new CountingBatchCallback<>(this,
				MapStorage.OP_deleteAll);
		this.deleteMultiCallback = countingBatchCallbackFactory.new CountingBatchCallback<>(this,
				MapStorage.OP_deleteMulti);
	}

	/*------------------------- map storage writer --------------------------*/

	@Override
	public void put(D databean, Config config){
		if(databean == null){
			return;
		}
		putMulti(Collections.singletonList(databean), config);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		if(databeans == null || databeans.isEmpty()){
			return;
		}
		String clientName = getClientId().getName();
		String nodeName = getName();
		Durability durability = HBaseConfigTool.getDurability(config);
		int batchSize = config.findInputBatchSize().orElse(DEFAULT_WRITE_BATCH_SIZE);
		for(List<D> databeanBatch : Scanner.of(databeans).batch(batchSize).iterable()){
			List<Row> actions = new ArrayList<>();
			int numCellsPut = 0;
			int putBytes = 0;
			int putValueBytes = 0;
			int numCellsDeleted = 0;
			int deleteBytes = 0;
			Map<EK,List<D>> databeansByEntityKey = EntityTool.getDatabeansByEntityKey(databeanBatch);
			for(Entry<EK,List<D>> ekAndPks : databeansByEntityKey.entrySet()){
				byte[] ekBytes = queryBuilder.getRowBytesWithPartition(ekAndPks.getKey());
				Put put = new Put(ekBytes);
				Delete delete = new Delete(ekBytes);
				for(D databean : ekAndPks.getValue()){
					PK pk = databean.getKey();
					byte[] qualifierPkBytes = queryBuilder.getQualifierPkBytes(pk, true);
					List<Field<?>> fields = getFieldInfo().getNonKeyFieldsWithValues(databean);
					boolean didAtLeastOneField = false;
					for(Field<?> field : fields){// TODO only put modified fields
						byte[] fullQualifierBytes = ByteTool.concatenate(getFieldInfo()
								.getEntityColumnPrefixBytes(), qualifierPkBytes, field.getKey()
								.getColumnNameBytes());
						byte[] fieldValueBytes = field.getBytes();
						if(fieldValueBytes == null){
							boolean ignoreNulls = config.findIgnoreNullFields().orElse(false);
							if(!ignoreNulls){
								delete.addColumns(FAM, fullQualifierBytes);
								deleteBytes += fullQualifierBytes.length;
								++numCellsDeleted;
							}
						}else{
							didAtLeastOneField = true;
							put.addColumn(FAM, fullQualifierBytes, fieldValueBytes);
							putBytes += fullQualifierBytes.length;
							putValueBytes += fieldValueBytes.length;
							++numCellsPut;
						}
					}
					if(!didAtLeastOneField){
						Field<?> dummyField = new SignedByteField(DUMMY, (byte)0);
						byte[] dummyQualifierBytes = ByteTool.concatenate(getFieldInfo()
								.getEntityColumnPrefixBytes(), qualifierPkBytes, dummyField.getKey()
										.getColumnNameBytes());
						byte[] dummyValueBytes = dummyField.getBytes();
						put.addColumn(FAM, dummyQualifierBytes, dummyValueBytes);
						putBytes += dummyQualifierBytes.length;
						putValueBytes += dummyValueBytes.length;
						++numCellsPut;
					}
				}
				if(!delete.isEmpty()){
					delete.setDurability(durability);
					actions.add(delete);
				}
				put.setDurability(durability);
				actions.add(put);
			}
			DatarouterCounters.incClientNodeCustom(clientType, "cells put", clientName, nodeName, numCellsPut);
			DatarouterCounters.incClientNodeCustom(clientType, "put", clientName, nodeName, 1);
			DatarouterCounters.incClientNodeCustom(clientType, "cells delete", clientName, nodeName, numCellsDeleted);
			DatarouterCounters.incClientNodeCustom(clientType, "delete", clientName, nodeName, 1);
			DatarouterCounters.incClientNodeCustom(clientType, "databeans put", clientName, nodeName, databeanBatch
					.size());
			DatarouterCounters.incClientNodeCustom(clientType, "entities put", clientName, nodeName,
					databeansByEntityKey.size());
			if(!actions.isEmpty()){
				try(Table table = getTable(); var $ = TracerTool.startSpan("Table batchCallback")){
					TracerTool.appendToSpanInfo(new TraceSpanInfoBuilder()
							.add("actions", actions.size())
							.add("cellsPut", numCellsPut)
							.add("putBytes", putBytes)
							.add("putValueBytes", putValueBytes)
							.add("cellsDeleted", numCellsDeleted)
							.add("deleteBytes", deleteBytes));
					table.batchCallback(actions, new Object[actions.size()], putMultiCallback);
				}catch(IOException | InterruptedException e){
					throw new RuntimeException(e);
				}
			}
		}
	}

	@Override
	public void increment(Map<PK,Map<String,Long>> countByColumnByKey, Config config){
		Durability durability = HBaseConfigTool.getDurability(config);
		List<Row> actions = new ArrayList<>();
		int cellCount = 0;
		int databeanCount = 0;
		Map<EK,List<PK>> keysByEntityKey = EntityTool.getPrimaryKeysByEntityKey(countByColumnByKey.keySet());
		for(Entry<EK,List<PK>> ekAndPks : keysByEntityKey.entrySet()){ //TODO: respect inputBatchSize
			byte[] ekBytes = queryBuilder.getRowBytesWithPartition(ekAndPks.getKey());
			Increment increment = new Increment(ekBytes);
			for(PK key : ekAndPks.getValue()){
				byte[] qualifierPkBytes = queryBuilder.getQualifierPkBytes(key, true);
				for(Entry<String, Long> entry : countByColumnByKey.get(key).entrySet()){
					assertColumnIsUInt63Field(entry.getKey());
					byte[] fullQualifierBytes = ByteTool.concatenate(getFieldInfo().getEntityColumnPrefixBytes(),
							qualifierPkBytes, StringByteTool.getUtf8Bytes(entry.getKey()));
					increment.addColumn(HBaseSubEntityNode.FAM, fullQualifierBytes, entry.getValue());
					++cellCount;
				}
				++databeanCount;
			}
			increment.setDurability(durability);
			actions.add(increment);
		}
		if(!actions.isEmpty()){
			try(Table table = getTable()){
				table.batch(actions, null);
			}catch(IOException | InterruptedException e){
				throw new RuntimeException(e);
			}
		}
		TracerTool.appendToSpanInfo(new TraceSpanInfoBuilder()
				.databeans(databeanCount)
				.add("cells", cellCount));
		String clientName = getClientId().getName();
		String nodeName = getName();
		DatarouterCounters.incClientNodeCustom(clientType, "cells incremented", clientName, nodeName, cellCount);
		DatarouterCounters.incClientNodeCustom(clientType, "databeans incremented", clientName, nodeName,
				databeanCount);
	}

	private void assertColumnIsUInt63Field(String columnName){
		Field<?> field = getFieldInfo().getFieldForColumnName(columnName);
		if(ObjectTool.notEquals(field.getClass(), UInt63Field.class)){
			throw new IllegalArgumentException(columnName + " is a " + field.getClass()
					+ ", but you can only increment a UInt63Field");
		}
	}

	@Override
	public void deleteAll(Config config){
		Durability durability = HBaseConfigTool.getDurability(config);
		Scan scan = new Scan();
		scan.setFilter(new ColumnPrefixFilter(getFieldInfo().getEntityColumnPrefixBytes()));
		try(Table table = getTable();
			ResultScanner managedResultScanner = HBaseTableTool.getResultScanner(table, scan)){
			Scanner.of(managedResultScanner)
					.exclude(Result::isEmpty)
					.map(HBaseSubEntityNode::makeDelete)
					.each(delete -> delete.setDurability(durability))
					.batch(100)
					.forEach(batch -> {
						try{
							table.batchCallback(batch, new Object[batch.size()], deleteAllCallback);
						}catch(IOException | InterruptedException e){
							throw new RuntimeException(e);
						}
					});
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	private static Delete makeDelete(Result result){
		Delete delete = new Delete(result.getRow());
		result.listCells().stream()
				.forEach(cell -> {
					byte[] family = CellUtil.cloneFamily(cell);
					byte[] qualifier = CellUtil.cloneQualifier(cell);
					delete.addColumns(family, qualifier);
				});
		return delete;
	}

	@Override
	public void delete(PK key, Config config){
		if(key == null){
			return;
		}
		deleteMulti(Collections.singletonList(key), config);
	}

	//TODO this only deletes columns known to the current fielder which could leave orphan columns from an old fielder
	@Override
	public void deleteMulti(Collection<PK> keys, Config config){
		if(keys == null || keys.isEmpty()){
			return;
		}
		Durability durability = HBaseConfigTool.getDurability(config);
		String clientName = getClientId().getName();
		String nodeName = getName();
		Collection<String> nonKeyColumnNames = getFieldInfo().getNonKeyFieldByColumnName().keySet();
		int batchSize = config.findInputBatchSize().orElse(DEFAULT_WRITE_BATCH_SIZE);
		for(List<PK> keyBatch : Scanner.of(keys).batch(batchSize).iterable()){
			Map<EK,List<PK>> pksByEk = EntityTool.getPrimaryKeysByEntityKey(keyBatch);
			ArrayList<Row> deletes = new ArrayList<>();// api requires ArrayList
			for(Entry<EK,List<PK>> ekAndPks : pksByEk.entrySet()){
				byte[] rowBytes = queryBuilder.getRowBytesWithPartition(ekAndPks.getKey());
				for(PK pk : ekAndPks.getValue()){
					for(String columnName : nonKeyColumnNames){// TODO only put modified fields
						Delete delete = new Delete(rowBytes);
						byte[] qualifier = queryBuilder.getQualifier(pk, columnName);
						delete.addColumns(FAM, qualifier);
						deletes.add(delete);
					}
					byte[] qualifierPkBytes = queryBuilder.getQualifierPkBytes(pk, true);
					Delete delete = new Delete(rowBytes);
					delete.setDurability(durability);
					Field<?> dummyField = new SignedByteField(DUMMY, (byte)0);
					byte[] dummyQualifierBytes = ByteTool.concatenate(getFieldInfo()
							.getEntityColumnPrefixBytes(), qualifierPkBytes, dummyField.getKey()
									.getColumnNameBytes());
					delete.addColumns(FAM, dummyQualifierBytes);
					deletes.add(delete);
				}
			}
			TracerTool.appendToSpanInfo(new TraceSpanInfoBuilder()
					.add("databeans", keyBatch.size())
					.add("deletes", deletes.size()));
			DatarouterCounters.incClientNodeCustom(clientType, "cells delete", clientName, nodeName, deletes.size());
			DatarouterCounters.incClientNodeCustom(clientType, "delete", clientName, nodeName, 1);
			DatarouterCounters.incClientNodeCustom(clientType, "databeans delete", clientName, nodeName, keyBatch
					.size());
			DatarouterCounters.incClientNodeCustom(clientType, "entities delete", clientName, nodeName, pksByEk.size());
			try(Table table = getTable()){
				table.batchCallback(deletes, new Object[deletes.size()], deleteMultiCallback);
			}catch(IOException | InterruptedException e){
				throw new RuntimeException(e);
			}
		}
	}

}
