package com.hotpads.datarouter.client.imp.hbase.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;

import com.hotpads.datarouter.client.imp.hbase.HBaseMultiAttemptTask;
import com.hotpads.datarouter.client.imp.hbase.HBaseTask;
import com.hotpads.datarouter.client.imp.hbase.factory.HBaseSimpleClientFactory;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.PhysicalSortedMapStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.comparable.ByteField;
import com.hotpads.datarouter.storage.key.KeyTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.profile.count.collection.Counters;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public class HBaseNode<PK extends PrimaryKey<PK>,D extends Databean<PK>> 
extends HBaseReaderNode<PK,D>
implements PhysicalSortedMapStorageNode<PK,D>
{
	
	public HBaseNode(Class<D> databeanClass, 
			DataRouter router, String clientName, 
			String physicalName, String qualifiedPhysicalName) {
		super(databeanClass, router, clientName, physicalName, qualifiedPhysicalName);
	}
	
	public HBaseNode(Class<D> databeanClass, 
			DataRouter router, String clientName) {
		super(databeanClass, router, clientName);
	}
	
	public HBaseNode(Class<? super D> baseDatabeanClass, Class<D> databeanClass, 
			DataRouter router, String clientName){
		super(baseDatabeanClass, databeanClass, router, clientName);
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
		putMulti(ListTool.wrap(databean), config);
	}

	
	@Override
	public void putMulti(final Collection<D> databeans, final Config pConfig) {
		if(CollectionTool.isEmpty(databeans)){ return; }
		final Config config = Config.nullSafe(pConfig);
		new HBaseMultiAttemptTask<Void>(new HBaseTask<Void>("putMulti", this, config){
				public Void wrappedCall() throws Exception{
					List<Put> puts = ListTool.createLinkedList();
					ArrayList<Delete> deletes = ListTool.createArrayList();//api requires ArrayList
					for(D databean : databeans){
						if(databean==null){ continue; }
						PK key = databean.getKey();
						byte[] keyBytes = key.getBytes(false);
						Put put = new Put(keyBytes);
						Delete delete = new Delete(keyBytes);
						List<Field<?>> fields = ListTool.nullSafeArray(databean.getNonKeyFields());
						for(Field<?> field : fields){//TODO only put modified fields
							byte[] fieldBytes = field.getBytes();
							if(fieldBytes==null){
								delete.deleteColumn(FAM, field.getMicroColumnNameBytes());
							}else{
								put.add(FAM, field.getMicroColumnNameBytes(), field.getBytes());
							}
						}
						if(put.isEmpty()){ 
							Field<?> dummyField = new ByteField(DUMMY, (byte)0);
							put.add(FAM, dummyField.getMicroColumnNameBytes(), dummyField.getBytes());
						}
						puts.add(put);
						if(!delete.isEmpty()){ deletes.add(delete); }
					}
					if(!config.getPersistentPut()){ disableWalForPuts(puts); }
					if(CollectionTool.notEmpty(puts)){ 
						hTable.put(puts); 
					}
					if(CollectionTool.notEmpty(deletes)){ 
						hTable.delete(deletes);
					}
					if(CollectionTool.notEmpty(puts) || CollectionTool.notEmpty(deletes)){
						hTable.flushCommits();
					}
					Counters.inc(node.getName()+" num cells put+delete", CollectionTool.size(puts)+CollectionTool.size(deletes));
					return null;
				}
			}).call();
	}
	
	
	@Override
	public void deleteAll(final Config pConfig) {
		final Config config = Config.nullSafe(pConfig);
		new HBaseMultiAttemptTask<Void>(new HBaseTask<Void>("deleteAll", this, config){
				public Void wrappedCall() throws Exception{
					ResultScanner scanner = hTable.getScanner(new Scan());
					ArrayList<Delete> batchToDelete = ListTool.createArrayList(1000);
					for(Result row : scanner){
						if(row.isEmpty()){ continue; }
						batchToDelete.add(new Delete(row.getRow()));
						if(batchToDelete.size() % 1000 == 0){
							hTable.delete(batchToDelete);
							hTable.flushCommits();
							batchToDelete.clear();
						}
					}
					if(CollectionTool.notEmpty(batchToDelete)){
						hTable.delete(batchToDelete);
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
		new HBaseMultiAttemptTask<Void>(new HBaseTask<Void>("deleteMulti", this, config){
				public Void wrappedCall() throws Exception{
					hTable.setAutoFlush(false);
					ArrayList<Delete> deletes = ListTool.createArrayListWithSize(keys);//api requires ArrayList
					for(PK key : keys){
						Delete delete = new Delete(key.getBytes(false));
						deletes.add(delete);
					}
					hTable.delete(deletes);
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
	
	public static void disableWalForPuts(Collection<Put> puts){
		for(Put put : CollectionTool.nullSafe(puts)){
			put.setWriteToWAL(false);
		}
	}

}
