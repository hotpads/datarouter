package com.hotpads.datarouter.client.imp.hbase.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.ColumnPrefixFilter;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.imp.hbase.task.HBaseMultiAttemptTask;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTask;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.entity.SubEntitySortedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.PhysicalSortedMapStorageNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.entity.EntityTool;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.comparable.SignedByteField;
import com.hotpads.datarouter.storage.key.KeyTool;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.util.core.BooleanTool;
import com.hotpads.util.core.ByteTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;

public class HBaseSubEntityNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends HBaseSubEntityReaderNode<EK,E,PK,D,F>
implements SubEntitySortedMapStorageNode<EK,PK,D,F>,
		PhysicalSortedMapStorageNode<PK,D>
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
		putMulti(ListTool.wrap(databean), config);
	}

	
	@Override
	public void putMulti(final Collection<D> databeans, final Config pConfig) {
		if(CollectionTool.isEmpty(databeans)){ return; }
		final Config config = Config.nullSafe(pConfig);
		new HBaseMultiAttemptTask<Void>(new HBaseTask<Void>(getDataRouterContext(), getTaskNameParams(), "putMulti", config){
				public Void hbaseCall() throws Exception{
					List<Row> actions = ListTool.createArrayList();
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
							for(Field<?> field : fields){//TODO only put modified fields
								byte[] fullQualifierBytes = ByteTool.concatenate(fieldInfo.getEntityColumnPrefixBytes(),
										qualifierPkBytes, field.getColumnNameBytes());
								byte[] fieldValueBytes = field.getBytes();
								if(fieldValueBytes==null){
									if(BooleanTool.isFalseOrNull(config.getIgnoreNullFields())){
										delete.deleteColumn(FAM, fullQualifierBytes, batchStartTime);
										++numCellsDeleted;
									}
								}else{
									put.add(FAM, fullQualifierBytes, fieldValueBytes);
									++numCellsPut;
								}
							}
							if(put.isEmpty()){ 
								Field<?> dummyField = new SignedByteField(DUMMY, (byte)0);
								byte[] dummyQualifierBytes = ByteTool.concatenate(fieldInfo.getEntityColumnPrefixBytes(),
										qualifierPkBytes, dummyField.getColumnNameBytes());
								put.add(FAM, dummyQualifierBytes, dummyField.getBytes());
							}
							put.setWriteToWAL(config.getPersistentPut());
							actions.add(put);
							if(!delete.isEmpty()){ actions.add(delete); }
						}
					}
					int numEntitiesPut = MapTool.size(databeansByEntityKey);
					int numDatabeansPut = CollectionTool.getTotalSizeOfMapOfCollections(databeansByEntityKey);
					DRCounters.incSuffixClientNode(client.getType(), "cells put", getClientName(), getNodeName(), numCellsPut);
					DRCounters.incSuffixClientNode(client.getType(), "cells delete", getClientName(), getNodeName(), numCellsDeleted);
					DRCounters.incSuffixClientNode(client.getType(), "databeans put", getClientName(), getNodeName(), numDatabeansPut);
					DRCounters.incSuffixClientNode(client.getType(), "entities put", getClientName(), getNodeName(), numEntitiesPut);
					if(CollectionTool.notEmpty(actions)){
						hTable.batch(actions);
						hTable.flushCommits();
					}
					return null;
				}
			}).call();
	}
	

	@Override
	public void deleteAll(final Config pConfig) {
		final Config config = Config.nullSafe(pConfig);
		new HBaseMultiAttemptTask<Void>(new HBaseTask<Void>(getDataRouterContext(), getTaskNameParams(), "deleteAll", config){
				public Void hbaseCall() throws Exception{
					Scan scan = new Scan();
					scan.setFilter(new ColumnPrefixFilter(fieldInfo.getEntityColumnPrefixBytes()));
					managedResultScanner = hTable.getScanner(scan);
					List<Row> batchToDelete = ListTool.createArrayList(1000);
					for(Result row : managedResultScanner){
						if(row.isEmpty()){ continue; }
						Delete delete = new Delete(row.getRow());
						for(KeyValue kv : row.list()){
							delete.deleteColumns(kv.getFamily(), kv.getQualifier());
						}
						batchToDelete.add(delete);
						if(batchToDelete.size() % 100 == 0){
							hTable.batch(batchToDelete);
							hTable.flushCommits();
							batchToDelete.clear();
						}
					}
					if(CollectionTool.notEmpty(batchToDelete)){
						hTable.batch(batchToDelete);
						hTable.flushCommits();
					}
					return null;
				}
			}).call();
	}

	
	@Override
	public void delete(PK key, Config pConfig) {
		deleteMulti(ListTool.wrap(key), pConfig);
	}

	
	//TODO this only deletes columns known to the current fielder.  could leave orphan columns from an old fielder
	@Override
	public void deleteMulti(final Collection<PK> keys, final Config pConfig){
		if(CollectionTool.isEmpty(keys)){ return; }
		final Config config = Config.nullSafe(pConfig);
		new HBaseMultiAttemptTask<Void>(new HBaseTask<Void>(getDataRouterContext(), getTaskNameParams(), "deleteMulti", config){
				public Void hbaseCall() throws Exception{
					hTable.setAutoFlush(false);
					Collection<String> nonKeyColumnNames = fieldInfo.getNonKeyFieldByColumnName().keySet();
					Map<EK,List<PK>> pksByEk = EntityTool.getPrimaryKeysByEntityKey(keys);
					ArrayList<Row> deletes = ListTool.createArrayList();//api requires ArrayList
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
		if(CollectionTool.notEmpty(databeansToDelete)){
			deleteMulti(KeyTool.getKeys(databeansToDelete), null);
		}
	}
	

}
