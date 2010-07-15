package com.hotpads.datarouter.client.imp.hbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.MapStorageNode;
import com.hotpads.datarouter.node.op.SortedStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.KeyTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.trace.TraceContext;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public class HBaseNode<PK extends PrimaryKey<PK>,D extends Databean<PK>> 
extends HBaseReaderNode<PK,D>
implements MapStorageNode<PK,D>
			,SortedStorageNode<PK,D>
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
	
	public static final byte[] FAM = HBaseClientFactory.DEFAULT_FAMILY_QUALIFIER;
	
	
	@Override
	public void put(final D databean, final Config config) {
		if(databean==null){ return; }
		putMulti(ListTool.wrap(databean), config);
	}

	
	@Override
	public void putMulti(Collection<D> databeans, final Config config) {
		if(CollectionTool.isEmpty(databeans)){ return; }
		TraceContext.startSpan(getName()+" putMulti");
		List<Put> puts = ListTool.createLinkedList();
		ArrayList<Delete> deletes = ListTool.createArrayList();//api requires ArrayList
		for(D databean : databeans){
			byte[] keyBytes = databean.getKey().getBytes(false);
//			logger.warn(this.getTableName()+" "+ByteTool.getBinaryStringBigEndian(keyBytes));
			Put put = new Put(keyBytes);
			Delete delete = new Delete(keyBytes);
			for(Field<?> field : databean.getNonKeyFields()){
				//TODO only put modified fields
				byte[] fieldBytes = field.getBytes();
				if(fieldBytes==null){
					delete.deleteColumn(FAM, field.getMicroNameBytes());
//					logger.warn("deleting "+databean.getKey()+":"+field.getMicroNameBytes());
				}else{
					put.add(FAM, field.getMicroNameBytes(), field.getBytes());
//					logger.warn("putting "+databean.getKey()+":"+field.getMicroNameBytes()+field.getValue());
				}
			}
			if(put.isEmpty()){ 
				throw new IllegalArgumentException("databean contained no fields so would be deleted:"+databean.getKey());
			}else{
				puts.add(put);
			}
			if(!delete.isEmpty()){ deletes.add(delete); }
		}
		HTable hTable = checkOutHTable();
		try{
			if(CollectionTool.notEmpty(puts)){ hTable.put(puts); }
			if(CollectionTool.notEmpty(deletes)){ hTable.delete(deletes); }
		}catch(IOException e){
			throw new DataAccessException(e);
		}finally{
			checkInHTable(hTable);//TODO wrap in executor to handle plumbing
			TraceContext.finishSpan();
		}
	}
	
	@Override
	public void deleteAll(final Config config) {
		TraceContext.startSpan(getName()+" deleteAll");
		HTable hTable = checkOutHTable();
		try{
			ResultScanner scanner = hTable.getScanner(new Scan());
			ArrayList<Delete> batchToDelete = ListTool.createArrayList(1000);
			for(Result row : scanner){
				if(row.isEmpty()){ continue; }
				batchToDelete.add(new Delete(row.getRow()));
				if(batchToDelete.size() % 1000 == 0){
					hTable.delete(batchToDelete);
					batchToDelete.clear();
				}
			}
			if(CollectionTool.notEmpty(batchToDelete)){
				hTable.delete(batchToDelete);
			}
				
//			List<D> batchToDelete = ListTool.createArrayList(1000);
//			Iterable<D> iterable = this.scan(null, true, null, true, null);
//			for(D d : iterable){
//				batchToDelete.add(d);
//				if(batchToDelete.size() % 1000 == 0){
//					this.deleteMulti(KeyTool.getKeys(batchToDelete), null);
//					batchToDelete.clear();
//				}
//			}
//			if(CollectionTool.notEmpty(batchToDelete)){
//				this.deleteMulti(KeyTool.getKeys(batchToDelete), null);
//			}
		}catch(IOException e){
			throw new DataAccessException(e);
		}finally{
			checkInHTable(hTable);//TODO wrap in executor to handle plumbing
			TraceContext.finishSpan();
		}
	}

	@Override
	public void delete(PK key, Config config) {
		TraceContext.startSpan(getName()+" delete");
		//this will not clear the databean from the hibernate session
		deleteMulti(ListTool.wrap(key), config);
		TraceContext.finishSpan();
	}

	@Override
	public void deleteMulti(final Collection<PK> keys, final Config config){
		if(CollectionTool.isEmpty(keys)){ return; }
		TraceContext.startSpan(getName()+" deleteMulti");
		ArrayList<Delete> deletes = ListTool.createArrayListWithSize(keys);//api requires ArrayList
		for(PK key : keys){
			Delete delete = new Delete(key.getBytes(false));
			deletes.add(delete);
		}
		HTable hTable = checkOutHTable();
		try{
			hTable.delete(deletes);
		}catch(IOException e){
			throw new DataAccessException(e);
		}finally{
			checkInHTable(hTable);//TODO wrap in executor to handle plumbing
			TraceContext.finishSpan();
		}
	}
	
	/************************** Sorted ************************************/

	@Override
	public void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		TraceContext.startSpan(getName()+" deleteAll");
		try{
			List<D> batchToDelete = this.getWithPrefix(prefix, wildcardLastField, config);
			if(CollectionTool.notEmpty(batchToDelete)){
				this.deleteMulti(KeyTool.getKeys(batchToDelete), null);
			}
		}finally{
			TraceContext.finishSpan();
		}
		
	}
	
	
	

}
