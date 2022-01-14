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
package io.datarouter.client.hbase.node.nonentity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.client.Table;

import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.client.hbase.HBaseClientManager;
import io.datarouter.client.hbase.callback.CountingBatchCallbackFactory;
import io.datarouter.client.hbase.callback.CountingBatchCallbackFactory.CountingBatchCallback;
import io.datarouter.client.hbase.config.DatarouterHBaseExecutors.DatarouterHbaseClientExecutor;
import io.datarouter.client.hbase.node.HBaseIncrement;
import io.datarouter.client.hbase.util.HBaseConfigTool;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.instrumentation.trace.TracerTool.TraceSpanInfoBuilder;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.comparable.SignedByteField;
import io.datarouter.model.field.imp.comparable.SignedByteFieldKey;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.op.combo.SortedMapStorage.PhysicalSortedMapStorageNode;
import io.datarouter.storage.node.op.raw.MapStorage;
import io.datarouter.storage.node.op.raw.write.StorageWriter;
import io.datarouter.storage.util.DatarouterCounters;
import io.datarouter.util.tuple.Range;

public class HBaseNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends HBaseReaderNode<EK,E,PK,D,F>
implements PhysicalSortedMapStorageNode<PK,D,F>, HBaseIncrement<PK>{

	public static final byte[] FAM = HBaseClientManager.DEFAULT_FAMILY_QUALIFIER;
	public static final SignedByteFieldKey DUMMY_FIELD_KEY = new SignedByteFieldKey(HBaseClientManager.DUMMY_COL_NAME);
	private static final SignedByteField DUMMY_FIELD = new SignedByteField(DUMMY_FIELD_KEY, (byte)0);

	private final CountingBatchCallback<?> putMultiCallback;
	private final CountingBatchCallback<?> deleteMultiCallback;
	private final ClientType<?,?> clientType;

	public HBaseNode(
			HBaseClientManager hBaseClientManager,
			ClientType<?,?> clientType,
			CountingBatchCallbackFactory countingBatchCallbackFactory,
			DatarouterHbaseClientExecutor datarouterHbaseClientExecutor,
			EntityNodeParams<EK,E> entityNodeParams,
			NodeParams<PK,D,F> params){
		super(hBaseClientManager, entityNodeParams, params, clientType, datarouterHbaseClientExecutor);
		this.clientType = clientType;
		this.putMultiCallback = countingBatchCallbackFactory.new CountingBatchCallback<>(this,
				StorageWriter.OP_putMulti);
		this.deleteMultiCallback = countingBatchCallbackFactory.new CountingBatchCallback<>(this,
				MapStorage.OP_deleteMulti);
	}

	/*------------------ StorageWriter --------------------*/

	@Override
	public void put(D databean, Config config){
		putMulti(Collections.singletonList(databean), config);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		if(databeans == null || databeans.isEmpty()){
			return;
		}
		Durability durability = HBaseConfigTool.getDurability(config);
		boolean ignoreNulls = config.findIgnoreNullFields().orElse(false);
		int batchSize = config.findInputBatchSize().orElse(100);
		Scanner.of(databeans)
				.include(Objects::nonNull)
				.map(databean -> makePutAndDelete(databean, ignoreNulls, durability))
				.batch(batchSize)
				.map(ActionBatch::new)
				.forEach(batch -> {
					try(var $ = TracerTool.startSpan("Table batchCallback", TraceSpanGroupType.DATABASE)){
						traceAndCount(batch.actions.size(), batch.numCellsPut, batch.numCellsDeleted, batch.putBytes,
								batch.putValueBytes, batch.deleteBytes);
						execute(batch.actions, putMultiCallback);
					}
				});
	}

	private PutAndDelete makePutAndDelete(D databean, boolean ignoreNulls, Durability durability){
		byte[] keyBytesWithPrefix = queryBuilder.getPkBytesWithPartition(databean.getKey());
		Put put = new Put(keyBytesWithPrefix).setDurability(durability);
		Delete delete = new Delete(keyBytesWithPrefix).setDurability(durability);
		int numCellsPut = 0;
		int numCellsDeleted = 0;
		int putBytes = 0;
		int putValueBytes = 0;
		int deleteBytes = 0;
		for(Field<?> field : getFieldInfo().getNonKeyFieldsWithValues(databean)){
			byte[] columnNameBytes = field.getKey().getColumnNameBytes();
			byte[] valueBytes = field.getBytes();
			if(valueBytes == null){
				if(!ignoreNulls){
					delete.addColumns(FAM, columnNameBytes);
					deleteBytes += columnNameBytes.length;
					++numCellsDeleted;
				}
			}else{
				put.addColumn(FAM, columnNameBytes, valueBytes);
				putBytes += columnNameBytes.length;
				putValueBytes += valueBytes.length;
				++numCellsPut;
			}
		}
		if(put.isEmpty()){
			byte[] columnNameBytes = DUMMY_FIELD_KEY.getColumnNameBytes();
			byte[] valueBytes = DUMMY_FIELD.getBytes();
			put.addColumn(FAM, columnNameBytes, valueBytes);
			putBytes += columnNameBytes.length;
			putValueBytes += valueBytes.length;
			++numCellsPut;
		}
		return new PutAndDelete(put, delete, numCellsPut, numCellsDeleted, putBytes, putValueBytes, deleteBytes);
	}

	private static class PutAndDelete{
		public final Put put;
		public final Delete delete;
		public final int numCellsPut;
		public final int numCellsDeleted;
		public final int putBytes;
		public final int putValueBytes;
		public final int deleteBytes;

		public PutAndDelete(Put put, Delete delete, int numCellsPut, int numCellsDeleted, int putBytes,
				int putValueBytes, int deleteBytes){
			this.put = put;
			this.delete = delete;
			this.numCellsPut = numCellsPut;
			this.numCellsDeleted = numCellsDeleted;
			this.putBytes = putBytes;
			this.putValueBytes = putValueBytes;
			this.deleteBytes = deleteBytes;
		}
	}

	private static class ActionBatch{
		public final List<Row> actions = new ArrayList<>();
		public int numCellsPut = 0;
		public int numCellsDeleted = 0;
		public int putBytes = 0;
		public int putValueBytes = 0;
		public int deleteBytes = 0;

		public ActionBatch(List<PutAndDelete> putAndDeletes){
			for(PutAndDelete putAndDelete : putAndDeletes){
				actions.add(putAndDelete.put);
				if(!putAndDelete.delete.isEmpty()){
					actions.add(putAndDelete.delete);
				}
				numCellsPut += putAndDelete.numCellsPut;
				numCellsDeleted += putAndDelete.numCellsDeleted;
				putBytes += putAndDelete.putBytes;
				putValueBytes += putAndDelete.putValueBytes;
				deleteBytes += putAndDelete.deleteBytes;
			}
		}
	}

	private void traceAndCount(int numActions, int numCellsPut, int numCellsDeleted, int putBytes, int putValueBytes,
			int deleteBytes){
		TracerTool.appendToSpanInfo(new TraceSpanInfoBuilder()
				.add("actions", numActions)
				.add("cellsPut", numCellsPut)
				.add("cellsDeleted", numCellsDeleted)
				.add("putBytes", putBytes)
				.add("putValueBytes", putValueBytes)
				.add("deleteBytes", deleteBytes));
		String clientName = clientTableNodeNames.getClientName();
		DatarouterCounters.incClientNodeCustom(clientType, "cells put", clientName, getName(), numCellsPut);
		DatarouterCounters.incClientNodeCustom(clientType, "cells delete", clientName, getName(), numCellsDeleted);
	}

	/*------------------ MapStorageWriter --------------------*/

	@Override
	public void deleteAll(Config config){
		Durability durability = HBaseConfigTool.getDurability(config);
		scanResults(Range.everything(), config, true)
				.map(Result::getRow)
				.map(Delete::new)
				.map(delete -> delete.setDurability(durability))
				.batch(config.findInputBatchSize().orElse(100))
				.forEach(actions -> execute(actions, deleteMultiCallback));
	}

	@Override
	public void delete(PK key, Config config){
		deleteMulti(Collections.singletonList(key), config);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config){
		Durability durability = HBaseConfigTool.getDurability(config);
		Scanner.of(keys)
				.map(queryBuilder::getPkBytesWithPartition)
				.map(Delete::new)
				.map(delete -> delete.setDurability(durability))
				.batch(config.findInputBatchSize().orElse(100))
				.forEach(deletes -> {
					TracerTool.appendToSpanInfo("databeans", deletes.size());
					execute(deletes, deleteMultiCallback);
				});
	}

	/*------------------ HBaseIncrement --------------------*/

	@Override
	public void increment(Map<PK,Map<String,Long>> countByColumnByKey, Config config){
		if(countByColumnByKey == null){
			return;
		}
		Durability durability = HBaseConfigTool.getDurability(config);
		List<Row> actions = new ArrayList<>();
		int cellCount = 0;
		int databeanCount = 0;
		for(Entry<PK,Map<String,Long>> row : countByColumnByKey.entrySet()){//TODO respect inputBatchSize
			byte[] keyBytesWithPrefix = queryBuilder.getPkBytesWithPartition(row.getKey());
			Increment increment = new Increment(keyBytesWithPrefix);
			for(Entry<String,Long> columnCount : row.getValue().entrySet()){
				String columnName = columnCount.getKey();
				byte[] columnNameBytes = StringCodec.UTF_8.encode(columnName);
				increment.addColumn(HBaseNode.FAM, columnNameBytes, columnCount.getValue());
				++cellCount;
			}
			increment.setDurability(durability);
			actions.add(increment);
			++databeanCount;
		}
		TracerTool.appendToSpanInfo(new TraceSpanInfoBuilder()
				.add("databeans", databeanCount)
				.add("cells", cellCount));
		String clientName = getClientId().getName();
		String nodeName = getName();
		DatarouterCounters.incClientNodeCustom(clientType, "cells incremented", clientName, nodeName, cellCount);
		DatarouterCounters.incClientNodeCustom(clientType, "databeans incremented", clientName, nodeName,
				databeanCount);
		if(!actions.isEmpty()){
			try(Table table = getTable()){
				table.batch(actions, null);
			}catch(IOException | InterruptedException e){
				throw new RuntimeException(e);
			}
		}
	}

	/*------------------ private --------------------*/

	private void execute(List<? extends Row> actions, CountingBatchCallback<?> callback){
		if(actions.isEmpty()){
			return;
		}
		try(Table table = getTable()){
			table.batchCallback(actions, new Object[actions.size()], callback);
		}catch(Exception e){
			throw new DataAccessException(e);
		}
	}

}
