package com.hotpads.datarouter.client.imp.hbase.node;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.client.Scan;

import com.hotpads.datarouter.client.imp.hbase.task.HBaseMultiAttemptTask;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTask;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseResultTool;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.PhysicalSortedMapStorageNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.comparable.SignedByteField;
import com.hotpads.datarouter.storage.key.KeyTool;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.util.core.BooleanTool;
import com.hotpads.util.core.ByteTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;

public class HBaseEntityNode<
		EK extends EntityKey<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends HBaseEntityReaderNode<EK,PK,D,F>
implements PhysicalSortedMapStorageNode<PK,D>
{
	
	public HBaseEntityNode(NodeParams<PK,D,F> params){
		super(params);
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
		new HBaseMultiAttemptTask<Void>(new HBaseTask<Void>(getDataRouterContext(), "putMulti", this, config){
				public Void hbaseCall() throws Exception{
					List<Row> actions = ListTool.createArrayList();
					int numCellsPut = 0, numCellsDeleted = 0;
					long batchStartTime = System.currentTimeMillis();
					Map<EK,List<D>> databeansByEntityKey = getDatabeansByEntityKey(databeans);
					for(EK ek : databeansByEntityKey.keySet()){
						byte[] ekBytes = getRowBytes(ek);
						Put put = new Put(ekBytes);
						Delete delete = new Delete(ekBytes);
						for(D databean : databeansByEntityKey.get(ek)){
							PK pk = databean.getKey();
							byte[] qualifierPkBytes = getQualifierPkBytes(pk);
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
					DRCounters.incSuffixClientNode(client.getType(), "cells put", getClientName(), node.getName(), numCellsPut);
					DRCounters.incSuffixClientNode(client.getType(), "cells delete", getClientName(), node.getName(), numCellsDeleted);
					DRCounters.incSuffixClientNode(client.getType(), "rows put", getClientName(), node.getName(), numDatabeansPut);
					DRCounters.incSuffixClientNode(client.getType(), "entities put", getClientName(), node.getName(), numEntitiesPut);
					if(CollectionTool.notEmpty(actions)){
						hTable.batch(actions);
						hTable.flushCommits();
					}
					return null;
				}
			}).call();
	}
	

	//alternative method would be to truncate the table
	@Override
	public void deleteAll(final Config pConfig) {
		final Config config = Config.nullSafe(pConfig);
		new HBaseMultiAttemptTask<Void>(new HBaseTask<Void>(getDataRouterContext(), "deleteAll", this, config){
				public Void hbaseCall() throws Exception{
					managedResultScanner = hTable.getScanner(new Scan());
					List<Row> batchToDelete = ListTool.createArrayList(1000);
					for(Result row : managedResultScanner){
						if(row.isEmpty()){ continue; }
						batchToDelete.add(new Delete(row.getRow()));
						if(batchToDelete.size() % 1000 == 0){
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

	
	@Override
	public void deleteMulti(final Collection<PK> keys, final Config pConfig){
		if(CollectionTool.isEmpty(keys)){ return; }
		final Config config = Config.nullSafe(pConfig);
		new HBaseMultiAttemptTask<Void>(new HBaseTask<Void>(getDataRouterContext(), "deleteMulti", this, config){
				public Void hbaseCall() throws Exception{
					hTable.setAutoFlush(false);
					List<Row> deletes = ListTool.createArrayListWithSize(keys);//api requires ArrayList
					for(PK key : keys){
						byte[] keyBytes = getRowBytesWithScatteringPrefix(null, key, false);
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
		if(CollectionTool.notEmpty(databeansToDelete)){
			deleteMulti(KeyTool.getKeys(databeansToDelete), null);
		}
	}
	
	
	/*************************** util **************************************/
	
	private Map<EK,List<D>> getDatabeansByEntityKey(Iterable<D> databeans){
		Map<EK,List<D>> databeansByEntityKey = MapTool.createTreeMap();
		for(D databean : IterableTool.nullSafe(databeans)){
			EK ek = databean.getKey().getEntityKey();
			List<D> databeansForEntity = databeansByEntityKey.get(ek);
			if(databeansForEntity==null){
				databeansForEntity = ListTool.createArrayList();
				databeansByEntityKey.put(ek, databeansForEntity);
			}
			databeansForEntity.add(databean);
		}
		return databeansByEntityKey;
	}
	
	public D getDatabean(Result row) {
		return HBaseResultTool.getDatabean(row, fieldInfo);
	}

}
