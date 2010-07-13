package com.hotpads.datarouter.client.imp.hbase;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.base.physical.BasePhysicalNode;
import com.hotpads.datarouter.node.op.MapStorageReaderNode;
import com.hotpads.datarouter.node.op.SortedStorageReaderNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.trace.TraceContext;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.exception.NotImplementedException;
import com.hotpads.util.core.iterable.PeekableIterable;

public class HBaseReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK>> 
extends BasePhysicalNode<PK,D>
implements PhysicalNode<PK,D>
			,MapStorageReaderNode<PK,D>
			,SortedStorageReaderNode<PK,D>
{
	protected Logger logger = Logger.getLogger(getClass());
	
	/******************************* constructors ************************************/

	public HBaseReaderNode(Class<D> databeanClass, 
			DataRouter router, String clientName, 
			String physicalName, String qualifiedPhysicalName) {
		super(databeanClass, router, clientName, physicalName, qualifiedPhysicalName);
	}
	
	public HBaseReaderNode(Class<D> databeanClass,
			DataRouter router, String clientName) {
		super(databeanClass, router, clientName);
	}

	public HBaseReaderNode(Class<? super D> baseDatabeanClass, Class<D> databeanClass, 
			DataRouter router, String clientName){
		super(baseDatabeanClass, databeanClass, router, clientName);
	}
	
	
	/***************************** plumbing methods ***********************************/

	@Override
	public HBaseClient getClient(){
		return (HBaseClient)this.router.getClient(getClientName());
	}
	
	@Override
	public Node<PK,D> getMaster() {
		return null;
	}
	
	@Override
	public void clearThreadSpecificState(){
	}

	public HTable checkOutHTable(){
		return this.getClient().checkOutHTable(this.getTableName());
	}
	
	public void checkInHTable(HTable hTable){
		this.getClient().checkInHTable(hTable);
	}
	
	/************************************ MapStorageReader methods ****************************/
	
	@Override
	public boolean exists(PK key, Config config) {
		return this.get(key, config) != null;
	}


	@Override
	public D get(final PK key, final Config config){
		if(key==null){ return null; }
		TraceContext.startSpan(getName()+" get");
		HTable hTable = checkOutHTable();
		try{
			Result row = hTable.get(new Get(key.getBytes()));
			if(row.isEmpty()){ return null; }
			D result = HBaseResultTool.getDatabean(row, databeanClass, primaryKeyFields, fieldByMicroName);
			return result;
		}catch(IOException e){
			throw new DataAccessException(e);
		}finally{
			checkInHTable(hTable);//TODO wrap in executor to handle plumbing
			TraceContext.finishSpan();
		}
	}
	
	
	@Override
	public List<D> getAll(final Config config){
		TraceContext.startSpan(getName()+" getAll");
		HTable hTable = checkOutHTable();
		try{
			List<D> results = ListTool.createArrayList();
			Scan scan = new Scan();
			scan.setCaching(HBaseQueryBuilder.getIterateBatchSize(config));
			ResultScanner scanner = hTable.getScanner(scan);
			for(Result row : scanner){
				if(row.isEmpty()){ continue; }
				D result = HBaseResultTool.getDatabean(row, databeanClass, primaryKeyFields, fieldByMicroName);
				results.add(result);
			}
			return results;
		}catch(IOException e){
			throw new DataAccessException(e);
		}finally{
			checkInHTable(hTable);//TODO wrap in executor to handle plumbing
			TraceContext.finishSpan();
		}
	}

	
	@Override
	public List<D> getMulti(final Collection<PK> keys, final Config config){	
		if(CollectionTool.isEmpty(keys)){ return new LinkedList<D>(); }
		TraceContext.startSpan(getName()+" getMulti");
		HTable hTable = checkOutHTable();
		try{
			List<D> results = ListTool.createArrayListWithSize(keys);
			for(PK key : keys){
				Result row = hTable.get(new Get(key.getBytes()));
				if(row.isEmpty()){ continue; }
				D result = HBaseResultTool.getDatabean(row, databeanClass, primaryKeyFields, fieldByMicroName);
				results.add(result);
			}
			return results;
		}catch(IOException e){
			throw new DataAccessException(e);
		}finally{
			checkInHTable(hTable);//TODO wrap in executor to handle plumbing
			TraceContext.finishSpan();
		}
	}
	
	@Override
	public List<PK> getKeys(final Collection<PK> keys, final Config config) {	
		if(CollectionTool.isEmpty(keys)){ return new LinkedList<PK>(); }
		TraceContext.startSpan(getName()+" getKeys");
		HTable hTable = checkOutHTable();
		try{
			List<PK> results = ListTool.createArrayListWithSize(keys);
			for(PK key : keys){
				Get get = new Get(key.getBytes());
				get.setFilter(new FirstKeyOnlyFilter());//make sure first column in row is not something big
				Result row = hTable.get(get);
				if(row.isEmpty()){ continue; }
				PK result = HBaseResultTool.getPrimaryKey(row, primaryKeyClass, primaryKeyFields);
				results.add(result);
			}
			return results;
		}catch(IOException e){
			throw new DataAccessException(e);
		}finally{
			checkInHTable(hTable);//TODO wrap in executor to handle plumbing
			TraceContext.finishSpan();
		}
	}

	
	/******************************* Sorted *************************************/
	
	@Override
	public PK getFirstKey(Config config){
		throw new NotImplementedException();
	}

	@Override
	public D getFirst(Config config){
		throw new NotImplementedException();
	}
	
	@Override
	public List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		TraceContext.startSpan(getName()+" getWithPrefix");
		HTable hTable = checkOutHTable();
		try{
			List<D> results = ListTool.createArrayList();
			Scan scan = HBaseQueryBuilder.getPrefixScanner(prefix, wildcardLastField, config);
			ResultScanner scanner = hTable.getScanner(scan);
			for(Result row : scanner){
				if(row.isEmpty()){ continue; }
				D result = HBaseResultTool.getDatabean(row, databeanClass, primaryKeyFields, fieldByMicroName);
				results.add(result);
			}
			return results;
		}catch(IOException e){
			throw new DataAccessException(e);
		}finally{
			checkInHTable(hTable);//TODO wrap in executor to handle plumbing
			TraceContext.finishSpan();
		}
	}

	@Override
	public List<D> getWithPrefixes(Collection<? extends PK> prefixes, boolean wildcardLastField, Config config){
		throw new NotImplementedException();
	}

	@Override
	public List<PK> getKeysInRange(PK start, boolean startInclusive, PK end, boolean endInclusive, Config config){
		throw new NotImplementedException();
	}

	@Override
	public List<D> getRange(PK start, boolean startInclusive, PK end, boolean endInclusive, Config config){
		throw new NotImplementedException();
	}
	
	@Override
	public List<D> getPrefixedRange(
			final PK prefix, final boolean wildcardLastField, 
			final PK end, final boolean endInclusive, 
			final Config config){
		throw new NotImplementedException();
	}

	@Override
	public PeekableIterable<D> scan(
			final PK start, final boolean startInclusive, 
			final PK end, final boolean endInclusive, 
			final Config config){
		throw new NotImplementedException();
	}
			
	
	
}
