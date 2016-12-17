package com.hotpads.datarouter.client.imp.hbase.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.ColumnPrefixFilter;

import com.hotpads.datarouter.client.DefaultClientTypes;
import com.hotpads.datarouter.client.imp.hbase.client.HBaseClient;
import com.hotpads.datarouter.client.imp.hbase.node.callback.CountingBatchCallback;
import com.hotpads.datarouter.client.imp.hbase.op.write.HBaseSubEntityIncrementOp;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseMultiAttemptTask;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTask;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.entity.SubEntitySortedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.PhysicalSortedMapStorageNode;
import com.hotpads.datarouter.node.op.index.HBaseIncrement;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.node.op.raw.write.StorageWriter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.entity.EntityTool;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.comparable.SignedByteField;
import com.hotpads.datarouter.storage.field.imp.comparable.SignedByteFieldKey;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.core.DrBooleanTool;
import com.hotpads.datarouter.util.core.DrByteTool;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrMapTool;

public class HBaseSubEntityNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends HBaseSubEntityReaderNode<EK,E,PK,D,F>
implements SubEntitySortedMapStorageNode<EK,PK,D,F>, PhysicalSortedMapStorageNode<PK,D>, HBaseIncrement<PK>{

	private final CountingBatchCallback<?> putMultiCallback;
	private final CountingBatchCallback<?> deleteAllCallback;
	private final CountingBatchCallback<?> deleteMultiCallback;

	public HBaseSubEntityNode(EntityNodeParams<EK,E> entityNodeParams, NodeParams<PK,D,F> params){
		super(entityNodeParams, params);
		//can't access "client" yet, so extract these strings from elsewhere
		String clientTypeString = DefaultClientTypes.CLIENT_TYPE_hbase;//TODO pass this in
		String clientName = getClientTableNodeNames().getClientName();
		this.putMultiCallback = new CountingBatchCallback<>(clientTypeString, clientName, getTableName(),
				StorageWriter.OP_putMulti);
		this.deleteAllCallback = new CountingBatchCallback<>(clientTypeString, clientName, getTableName(),
				MapStorage.OP_deleteAll);
		this.deleteMultiCallback = new CountingBatchCallback<>(clientTypeString, clientName, getTableName(),
				MapStorage.OP_deleteMulti);
	}

	@Override
	public Node<PK,D> getMaster(){
		return this;
	}


	/************************************ MapStorageWriter methods ****************************/

	public static final byte[] FAM = HBaseNode.FAM;
	public static final SignedByteFieldKey DUMMY = HBaseNode.DUMMY;


	@Override
	public void put(final D databean, final Config config){
		if(databean == null){
			return;
		}
		putMulti(DrListTool.wrap(databean), config);
	}


	@Override
	public void putMulti(final Collection<D> databeans, final Config config){
		if(DrCollectionTool.isEmpty(databeans)){
			return;
		}
		new HBaseMultiAttemptTask<>(new HBaseTask<Void>(getDatarouter(), getClientTableNodeNames(), "putMulti",
				Config.nullSafe(config)){
			@Override
			public Void hbaseCall(Table table, HBaseClient client, ResultScanner managedResultScanner)
			throws Exception{
				List<Row> actions = new ArrayList<>();
				int numCellsPut = 0, numCellsDeleted = 0;
				long batchStartTime = System.currentTimeMillis();
				Map<EK,List<D>> databeansByEntityKey = EntityTool.getDatabeansByEntityKey(databeans);
				for(EK ek : databeansByEntityKey.keySet()){
					byte[] ekBytes = queryBuilder.getRowBytesWithPartition(ek);
					Put put = new Put(ekBytes);
					Delete delete = new Delete(ekBytes);
					for(D databean : databeansByEntityKey.get(ek)){
						PK pk = databean.getKey();
						byte[] qualifierPkBytes = queryBuilder.getQualifierPkBytes(pk, true);
						List<Field<?>> fields = fieldInfo.getNonKeyFieldsWithValues(databean);
						boolean didAtLeastOneField = false;
						for(Field<?> field : fields){//TODO only put modified fields
							byte[] fullQualifierBytes = DrByteTool.concatenate(fieldInfo.getEntityColumnPrefixBytes(),
									qualifierPkBytes, field.getKey().getColumnNameBytes());
							byte[] fieldValueBytes = field.getBytes();
							if(fieldValueBytes == null){
								if(DrBooleanTool.isFalseOrNull(config.getIgnoreNullFields())){
									delete.deleteColumn(FAM, fullQualifierBytes, batchStartTime);
									++numCellsDeleted;
								}
							}else{
								didAtLeastOneField = true;
								put.add(FAM, fullQualifierBytes, fieldValueBytes);
								++numCellsPut;
							}
						}
						if(!didAtLeastOneField){
							Field<?> dummyField = new SignedByteField(DUMMY, (byte)0);
							byte[] dummyQualifierBytes = DrByteTool.concatenate(fieldInfo.getEntityColumnPrefixBytes(),
									qualifierPkBytes, dummyField.getKey().getColumnNameBytes());
							put.add(FAM, dummyQualifierBytes, dummyField.getBytes());
						}
						if(!delete.isEmpty()){
							actions.add(delete);
						}
					}
					put.setWriteToWAL(config.getPersistentPut());
					actions.add(put);
				}
				int numEntitiesPut = DrMapTool.size(databeansByEntityKey);
				int numDatabeansPut = DrCollectionTool.getTotalSizeOfMapOfCollections(databeansByEntityKey);
				DRCounters.incClientNodeCustom(client.getType(), "cells put", getClientName(), getNodeName(),
						numCellsPut);
				DRCounters.incClientNodeCustom(client.getType(), "cells delete", getClientName(), getNodeName(),
						numCellsDeleted);
				DRCounters.incClientNodeCustom(client.getType(), "databeans put", getClientName(), getNodeName(),
						numDatabeansPut);
				DRCounters.incClientNodeCustom(client.getType(), "entities put", getClientName(), getNodeName(),
						numEntitiesPut);
				if(DrCollectionTool.notEmpty(actions)){
					table.batchCallback(actions, new Object[actions.size()], putMultiCallback);
				}
				return null;
			}
		}).call();
	}

	@Override
	public void increment(Map<PK,Map<String,Long>> countByColumnByKey, Config config){
		new HBaseMultiAttemptTask<>(new HBaseSubEntityIncrementOp<>(this, countByColumnByKey, Config.nullSafe(config),
				queryBuilder)).call();
	}

	@Override
	public void deleteAll(final Config config){
		new HBaseMultiAttemptTask<>(new HBaseTask<Void>(getDatarouter(), getClientTableNodeNames(), "deleteAll",
				Config.nullSafe(config)){
			@Override
			public Void hbaseCall(Table table, HBaseClient client, ResultScanner managedResultScanner)
			throws Exception{
				Scan scan = new Scan();
				scan.setFilter(new ColumnPrefixFilter(fieldInfo.getEntityColumnPrefixBytes()));
				managedResultScanner = table.getScanner(scan);
				List<Row> batchToDelete = new ArrayList<>(1000);
				for(Result row : managedResultScanner){
					if(row.isEmpty()){
						continue;
					}
					Delete delete = new Delete(row.getRow());
					for(KeyValue kv : DrIterableTool.nullSafe(row.list())){//row.list() can return null
						delete.deleteColumns(kv.getFamily(), kv.getQualifier());
					}
					batchToDelete.add(delete);
					if(batchToDelete.size() % 100 == 0){
						table.batchCallback(batchToDelete, new Object[batchToDelete.size()], deleteAllCallback);
						batchToDelete.clear();
					}
				}
				if(DrCollectionTool.notEmpty(batchToDelete)){
					table.batchCallback(batchToDelete, new Object[batchToDelete.size()], deleteAllCallback);
				}
				return null;
			}
		}).call();
	}


	@Override
	public void delete(PK key, Config config){
		deleteMulti(DrListTool.wrap(key), config);
	}


	//TODO this only deletes columns known to the current fielder.  could leave orphan columns from an old fielder
	@Override
	public void deleteMulti(final Collection<PK> keys, final Config config){
		if(DrCollectionTool.isEmpty(keys)){
			return;
		}
		new HBaseMultiAttemptTask<>(new HBaseTask<Void>(getDatarouter(), getClientTableNodeNames(), "deleteMulti",
				Config.nullSafe(config)){
			@Override
			public Void hbaseCall(Table table, HBaseClient client, ResultScanner managedResultScanner)
			throws Exception{
				Collection<String> nonKeyColumnNames = fieldInfo.getNonKeyFieldByColumnName().keySet();
				Map<EK,List<PK>> pksByEk = EntityTool.getPrimaryKeysByEntityKey(keys);
				ArrayList<Row> deletes = new ArrayList<>();//api requires ArrayList
				for(EK ek : pksByEk.keySet()){
					byte[] rowBytes = queryBuilder.getRowBytesWithPartition(ek);
					for(PK pk : pksByEk.get(ek)){
						for(String columnName : nonKeyColumnNames){//TODO only put modified fields
							Delete delete = new Delete(rowBytes);
							byte[] qualifier = queryBuilder.getQualifier(pk, columnName);
							delete.deleteColumns(FAM, qualifier);
							deletes.add(delete);
						}
						byte[] qualifierPkBytes = queryBuilder.getQualifierPkBytes(pk, true);
						Delete delete = new Delete(rowBytes);
						Field<?> dummyField = new SignedByteField(DUMMY, (byte)0);
						byte[] dummyQualifierBytes = DrByteTool.concatenate(fieldInfo.getEntityColumnPrefixBytes(),
								qualifierPkBytes, dummyField.getKey().getColumnNameBytes());
						delete.deleteColumns(FAM, dummyQualifierBytes);
						deletes.add(delete);
					}
				}
				table.batchCallback(deletes, new Object[deletes.size()], deleteMultiCallback);
				return null;
			}
		}).call();
	}

}
