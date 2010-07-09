package com.hotpads.datarouter.client.imp.hbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.MapStorageNode;
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
	
	@Override
	public void put(final D databean, final Config config) {
		if(databean==null){ return; }
		TraceContext.startSpan(getName()+" put");
		Put put = new Put(databean.getKey().getBytes());
		for(Field<?> field : databean.getNonKeyFields()){
			//TODO only put modified fields
			put.add(new byte[0], field.getMicroNameBytes(), field.getBytes());
		}
		HTable hTable = checkOutHTable();
		try{
			hTable.put(put);
		}catch(IOException e){
			throw new DataAccessException(e);
		}finally{
			checkInHTable(hTable);//TODO wrap in executor to handle plumbing
			TraceContext.finishSpan();
		}
	}

	
	@Override
	public void putMulti(Collection<D> databeans, final Config config) {
		if(CollectionTool.isEmpty(databeans)){ return; }
		TraceContext.startSpan(getName()+" putMulti");
		List<Put> puts = ListTool.createArrayListWithSize(databeans);
		for(D databean : databeans){
			Put put = new Put(databean.getKey().getBytes());
			for(Field<?> field : databean.getNonKeyFields()){
				//TODO only put modified fields
				put.add(new byte[0], field.getMicroNameBytes(), field.getBytes());
			}
			puts.add(put);
		}
		HTable hTable = checkOutHTable();
		try{
			hTable.put(puts);
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
		try{
			//TODO make this reasonable... use an iterator
			List<D> all = this.getAll(null);
			this.deleteMulti(KeyTool.getKeys(all), null);
		}finally{
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

	/*
	 * deleting 1000 rows by PK from a table with no indexes takes 200ms when executed as one statement
	 *  and 600ms when executed as 1000 batch deletes in a transaction
	 *  
	 * make sure MySQL's max packet size is big.  it may default to 1MB... set to like 64MB
	 * 
	 */
	@Override
	public void deleteMulti(final Collection<PK> keys, final Config config){
		if(CollectionTool.isEmpty(keys)){ return; }
		TraceContext.startSpan(getName()+" deleteMulti");
		ArrayList<Delete> deletes = ListTool.createArrayListWithSize(keys);
		for(PK key : keys){
			Delete delete = new Delete(key.getBytes());
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

	
}
