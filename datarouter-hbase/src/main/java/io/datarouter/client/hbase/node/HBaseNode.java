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
package io.datarouter.client.hbase.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.client.Table;

import io.datarouter.client.hbase.HBaseClientManager;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.instrumentation.trace.TracerTool.TraceSpanInfoBuilder;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.op.combo.SortedMapStorage.PhysicalSortedMapStorageNode;
import io.datarouter.storage.util.DatarouterCounters;
import io.datarouter.util.tuple.Range;

public class HBaseNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends HBaseReaderNode<EK,E,PK,D,F>
implements PhysicalSortedMapStorageNode<PK,D,F>{

	private final ClientType<?,?> clientType;

	public HBaseNode(
			HBaseClientManager hBaseClientManager,
			ClientType<?,?> clientType,
			EntityNodeParams<EK,E> entityNodeParams,
			NodeParams<PK,D,F> params){
		super(hBaseClientManager, entityNodeParams, params, clientType);
		this.clientType = clientType;
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
		int batchSize = config.findRequestBatchSize().orElse(100);
		Scanner.of(databeans)
				.include(Objects::nonNull)
				.map(databean -> makePutAndDelete(databean))
				.batch(batchSize)
				.map(ActionBatch::new)
				.forEach(batch -> {
					try(var $ = TracerTool.startSpan("Table batchCallback", TraceSpanGroupType.DATABASE)){
						traceAndCount(
								batch.actions.size(),
								batch.numCellsPut,
								batch.numCellsDeleted,
								batch.putBytes,
								batch.putValueBytes,
								batch.deleteBytes);
						execute(batch.actions);
					}
				});
	}

	private PutAndDelete makePutAndDelete(D databean){
		byte[] keyBytes = queryBuilder.getPkBytes(databean.getKey());
		Put put = new Put(keyBytes);
		Delete delete = new Delete(keyBytes);
		int numCellsPut = 0;
		int numCellsDeleted = 0;
		int putBytes = 0;
		int putValueBytes = 0;
		int deleteBytes = 0;
		for(Field<?> field : getFieldInfo().getNonKeyFieldsWithValues(databean)){
			field.getKey().getColumnNameBytes();
			byte[] columnNameBytes = field.getKey().getColumnNameBytes();
			byte[] valueBytes = field.getValueBytes();
			if(valueBytes == null){
				delete.addColumns(HBaseClientManager.DEFAULT_FAMILY_QUALIFIER, columnNameBytes);
				deleteBytes += columnNameBytes.length;
				++numCellsDeleted;
			}else{
				put.addColumn(HBaseClientManager.DEFAULT_FAMILY_QUALIFIER, columnNameBytes, valueBytes);
				putBytes += columnNameBytes.length;
				putValueBytes += valueBytes.length;
				++numCellsPut;
			}
		}
		if(put.isEmpty()){
			byte[] columnNameBytes = HBaseClientManager.DUMMY_COL_NAME_BYTES;
			byte[] valueBytes = HBaseClientManager.DUMMY_FIELD_VALUE;
			put.addColumn(HBaseClientManager.DEFAULT_FAMILY_QUALIFIER, columnNameBytes, valueBytes);
			putBytes += columnNameBytes.length;
			putValueBytes += valueBytes.length;
			++numCellsPut;
		}
		return new PutAndDelete(put, delete, numCellsPut, numCellsDeleted, putBytes, putValueBytes, deleteBytes);
	}

	private record PutAndDelete(
			Put put,
			Delete delete,
			int numCellsPut,
			int numCellsDeleted,
			int putBytes,
			int putValueBytes,
			int deleteBytes){
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

	private void traceAndCount(
			int numActions,
			int numCellsPut,
			int numCellsDeleted,
			int putBytes,
			int putValueBytes,
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
		scanResults(Range.everything(), config, true)
				.map(Result::getRow)
				.map(Delete::new)
				.batch(config.findRequestBatchSize().orElse(100))
				.forEach(actions -> execute(actions));
	}

	@Override
	public void delete(PK key, Config config){
		deleteMulti(Collections.singletonList(key), config);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config){
		Scanner.of(keys)
				.map(queryBuilder::getPkBytes)
				.map(Delete::new)
				.batch(config.findRequestBatchSize().orElse(100))
				.forEach(deletes -> {
					TracerTool.appendToSpanInfo("databeans", deletes.size());
					execute(deletes);
				});
	}

	/*------------------ private --------------------*/

	private void execute(List<? extends Row> actions){
		if(actions.isEmpty()){
			return;
		}
		try(Table table = getTable()){
			table.batch(actions, new Object[actions.size()]);
		}catch(Exception e){
			throw new DataAccessException(e);
		}
	}

}
