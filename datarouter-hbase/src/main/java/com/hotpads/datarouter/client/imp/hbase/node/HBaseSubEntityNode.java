package com.hotpads.datarouter.client.imp.hbase.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.ColumnPrefixFilter;

import com.hotpads.datarouter.client.imp.hbase.client.HBaseClient;
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
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.databean.DatabeanTool;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.entity.EntityTool;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.comparable.SignedByteField;
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
implements SubEntitySortedMapStorageNode<EK,PK,D,F>,
		PhysicalSortedMapStorageNode<PK,D>, HBaseIncrement<PK>
{

	public HBaseSubEntityNode(EntityNodeParams<EK,E> entityNodeParams, NodeParams<PK,D,F> params){
		super(entityNodeParams, params);
	}

	@Override
	public Node<PK,D> getMaster() {
		return this;
	}


	/************************************ MapStorageWriter methods ****************************/

	public static final byte[] FAM = HBaseNode.FAM;
	public static final String DUMMY = HBaseNode.DUMMY;


	@Override
	public void put(final D databean, final Config config) {
		if(databean==null){ return; }
		putMulti(DrListTool.wrap(databean), config);
	}


	@Override
	public void putMulti(final Collection<D> databeans, final Config pConfig) {
		if(DrCollectionTool.isEmpty(databeans)){ return; }
		final Config config = Config.nullSafe(pConfig);
		new HBaseMultiAttemptTask<Void>(new HBaseTask<Void>(getDatarouter(), getClientTableNodeNames(), "putMulti",
				config){
				@Override
				public Void hbaseCall(HTable hTable, HBaseClient client, ResultScanner managedResultScanner)
				throws Exception{
//					PhaseTimer timer = new PhaseTimer();
					List<Row> actions = new ArrayList<>();
					int numCellsPut = 0, numCellsDeleted = 0;
					long batchStartTime = System.currentTimeMillis();
					Map<EK,List<D>> databeansByEntityKey = EntityTool.getDatabeansByEntityKey(databeans);
//					timer.add("group by EK "+MapTool.size(databeansByEntityKey));
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
								didAtLeastOneField = true;
								byte[] fullQualifierBytes = DrByteTool.concatenate(fieldInfo.getEntityColumnPrefixBytes(),
										qualifierPkBytes, field.getKey().getColumnNameBytes());
								byte[] fieldValueBytes = field.getBytes();
								if(fieldValueBytes==null){
									if(DrBooleanTool.isFalseOrNull(config.getIgnoreNullFields())){
										delete.deleteColumn(FAM, fullQualifierBytes, batchStartTime);
										++numCellsDeleted;
									}
								}else{
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
							if(!delete.isEmpty()){ actions.add(delete); }
						}
						put.setWriteToWAL(config.getPersistentPut());
						actions.add(put);
					}
					int numEntitiesPut = DrMapTool.size(databeansByEntityKey);
					int numDatabeansPut = DrCollectionTool.getTotalSizeOfMapOfCollections(databeansByEntityKey);
					DRCounters.incClientNodeCustom(client.getType(), "cells put", getClientName(), getNodeName(), numCellsPut);
					DRCounters.incClientNodeCustom(client.getType(), "cells delete", getClientName(), getNodeName(), numCellsDeleted);
					DRCounters.incClientNodeCustom(client.getType(), "databeans put", getClientName(), getNodeName(), numDatabeansPut);
					DRCounters.incClientNodeCustom(client.getType(), "entities put", getClientName(), getNodeName(), numEntitiesPut);
//					timer.add("built puts "+CollectionTool.size(actions));
					if(DrCollectionTool.notEmpty(actions)){
						hTable.batch(actions);
//						timer.add("batch");
						hTable.flushCommits();
//						timer.add("flush");
					}
//					logger.warn(timer.toString());
					return null;
				}
			}).call();
	}

	@Override
	public void increment(Map<PK,Map<String,Long>> countByColumnByKey, Config pConfig){
		final Config config = Config.nullSafe(pConfig);
		new HBaseMultiAttemptTask<Void>(new HBaseSubEntityIncrementOp<EK, E, PK, D, F>(this, countByColumnByKey, config,
				queryBuilder)).call();
	}

	@Override
	public void deleteAll(final Config pConfig) {
		final Config config = Config.nullSafe(pConfig);
		new HBaseMultiAttemptTask<Void>(new HBaseTask<Void>(getDatarouter(), getClientTableNodeNames(), "deleteAll", config){
				@Override
				public Void hbaseCall(HTable hTable, HBaseClient client, ResultScanner managedResultScanner) throws Exception{
					Scan scan = new Scan();
					scan.setFilter(new ColumnPrefixFilter(fieldInfo.getEntityColumnPrefixBytes()));
					managedResultScanner = hTable.getScanner(scan);
					List<Row> batchToDelete = new ArrayList<>(1000);
					for(Result row : managedResultScanner){
						if(row.isEmpty()){ continue; }
						Delete delete = new Delete(row.getRow());
						for(KeyValue kv : DrIterableTool.nullSafe(row.list())){//row.list() can return null
							delete.deleteColumns(kv.getFamily(), kv.getQualifier());
						}
						batchToDelete.add(delete);
						if(batchToDelete.size() % 100 == 0){
							hTable.batch(batchToDelete);
							hTable.flushCommits();
							batchToDelete.clear();
						}
					}
					if(DrCollectionTool.notEmpty(batchToDelete)){
						hTable.batch(batchToDelete);
						hTable.flushCommits();
					}
					return null;
				}
			}).call();
	}


	@Override
	public void delete(PK key, Config pConfig) {
		deleteMulti(DrListTool.wrap(key), pConfig);
	}


	//TODO this only deletes columns known to the current fielder.  could leave orphan columns from an old fielder
	@Override
	public void deleteMulti(final Collection<PK> keys, final Config pConfig){
		if(DrCollectionTool.isEmpty(keys)){ return; }
		final Config config = Config.nullSafe(pConfig);
		new HBaseMultiAttemptTask<Void>(new HBaseTask<Void>(getDatarouter(), getClientTableNodeNames(), "deleteMulti", config){
				@Override
				public Void hbaseCall(HTable hTable, HBaseClient client, ResultScanner managedResultScanner) throws Exception{
					hTable.setAutoFlush(false);
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
						}
					}
					hTable.batch(deletes);
					hTable.flushCommits();
					return null;
				}
			}).call();
	}


	/************************** Sorted ************************************/

	@Override
	public void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		//TODO need a method getKeysWithPrefix
		List<D> databeansToDelete = getWithPrefix(prefix, wildcardLastField, config);
		if(DrCollectionTool.notEmpty(databeansToDelete)){
			deleteMulti(DatabeanTool.getKeys(databeansToDelete), null);
		}
	}


}
