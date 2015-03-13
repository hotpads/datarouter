package com.hotpads.datarouter.client.imp.hbase.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.client.Scan;

import com.hotpads.datarouter.client.imp.hbase.factory.HBaseSimpleClientFactory;
import com.hotpads.datarouter.client.imp.hbase.op.write.HBaseIncrementOp;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseMultiAttemptTask;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTask;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseResultTool;
import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.PhysicalSortedMapStorageNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.comparable.SignedByteField;
import com.hotpads.datarouter.storage.key.KeyTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.core.DrBooleanTool;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;

public class HBaseNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends HBaseReaderNode<PK,D,F>
implements PhysicalSortedMapStorageNode<PK,D>
{
	
	public HBaseNode(NodeParams<PK,D,F> params){
		super(params);
	}
	
	@Override
	public Node<PK,D> getMaster() {
		return this;
	}
	
	
	/************************************ MapStorageWriter methods ****************************/
	
	public static final byte[] FAM = HBaseSimpleClientFactory.DEFAULT_FAMILY_QUALIFIER;
	public static final String DUMMY = HBaseSimpleClientFactory.DUMMY_COL_NAME;
	
	
	@Override
	public void put(final D databean, final Config config) {
		if(databean==null){ return; }
		putMulti(DrListTool.wrap(databean), config);
	}

	
	@Override
	public void putMulti(final Collection<D> databeans, final Config pConfig) {
		if(DrCollectionTool.isEmpty(databeans)){ return; }
		final Config config = Config.nullSafe(pConfig);
		new HBaseMultiAttemptTask<Void>(new HBaseTask<Void>(getDatarouterContext(), getTaskNameParams(), "putMulti", config){
				public Void hbaseCall(HTable hTable, HBaseClient client, ResultScanner managedResultScanner) throws Exception{					
					List<Row> actions = new ArrayList<>();
					int numCellsPut = 0, numCellsDeleted = 0, numRowsPut = 0;;
					long batchStartTime = System.currentTimeMillis();
					for(D databean : databeans){//TODO obey Config.commitBatchSize
						if(databean==null){ continue; }
						PK key = databean.getKey();
						byte[] keyBytes = getKeyBytesWithScatteringPrefix(null, key, false);
						Put put = new Put(keyBytes);
						Delete delete = new Delete(keyBytes);
						List<Field<?>> fields = fieldInfo.getNonKeyFieldsWithValues(databean);
						for(Field<?> field : fields){//TODO only put modified fields
							byte[] fieldBytes = field.getBytes();
							if(fieldBytes==null){
								if(DrBooleanTool.isFalseOrNull(config.getIgnoreNullFields())){
									delete.deleteColumn(FAM, field.getColumnNameBytes(), batchStartTime);
									++numCellsDeleted;
								}
							}else{
								put.add(FAM, field.getColumnNameBytes(), field.getBytes());
								++numCellsPut;
							}
						}
						if(put.isEmpty()){ 
							Field<?> dummyField = new SignedByteField(DUMMY, (byte)0);
							put.add(FAM, dummyField.getColumnNameBytes(), dummyField.getBytes());
						}
						put.setWriteToWAL(config.getPersistentPut());
						actions.add(put);
						if(!delete.isEmpty()){ actions.add(delete); }
						++numRowsPut;
					}
					DRCounters.incSuffixClientNode(client.getType(), "cells put", getClientName(), getName(), numCellsPut);
					DRCounters.incSuffixClientNode(client.getType(), "cells delete", getClientName(), getName(), numCellsDeleted);
					DRCounters.incSuffixClientNode(client.getType(), "rows put", getClientName(), getName(), numRowsPut);
					if(DrCollectionTool.notEmpty(actions)){
						hTable.batch(actions);
						hTable.flushCommits();
					}
					return null;
				}
			}).call();
	}
	
//	@Override  //not in the parent interface yet
	public void increment(Map<PK,Map<String,Long>> countByColumnByKey, Config pConfig){
		final Config config = Config.nullSafe(pConfig);
		new HBaseMultiAttemptTask<Void>(new HBaseIncrementOp<PK,D,F>(this, countByColumnByKey, config)).call();
	}
	

	//alternative method would be to truncate the table
	@Override
	public void deleteAll(final Config pConfig) {
		final Config config = Config.nullSafe(pConfig);
		new HBaseMultiAttemptTask<Void>(new HBaseTask<Void>(getDatarouterContext(), getTaskNameParams(), "deleteAll", config){
				public Void hbaseCall(HTable hTable, HBaseClient client, ResultScanner managedResultScanner) throws Exception{
					managedResultScanner = hTable.getScanner(new Scan());
					List<Row> batchToDelete = new ArrayList<>(1000);
					for(Result row : managedResultScanner){
						if(row.isEmpty()){ continue; }
						batchToDelete.add(new Delete(row.getRow()));
						if(batchToDelete.size() % 1000 == 0){
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

	
	@Override
	public void deleteMulti(final Collection<PK> keys, final Config pConfig){
		if(DrCollectionTool.isEmpty(keys)){ return; }
		final Config config = Config.nullSafe(pConfig);
		new HBaseMultiAttemptTask<Void>(new HBaseTask<Void>(getDatarouterContext(), getTaskNameParams(), "deleteMulti", config){
				public Void hbaseCall(HTable hTable, HBaseClient client, ResultScanner managedResultScanner) throws Exception{
					hTable.setAutoFlush(false);
					List<Row> deletes = DrListTool.createArrayListWithSize(keys);//api requires ArrayList
					for(PK key : keys){
						byte[] keyBytes = getKeyBytesWithScatteringPrefix(null, key, false);
						Delete delete = new Delete(keyBytes);
//						Delete delete = new Delete(key.getBytes(false));
						deletes.add(delete);
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
			deleteMulti(KeyTool.getKeys(databeansToDelete), null);
		}
	}
	
	
	/*************************** util **************************************/
	
	public D getDatabean(Result row) {
		return HBaseResultTool.getDatabean(row, fieldInfo);
	}

}
