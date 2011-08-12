package com.hotpads.datarouter.client.imp.hbase;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.NoServerForRegionException;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.bytes.StringByteTool;

public class HTablePool{
	protected Logger logger = Logger.getLogger(getClass());
	
	protected Long lastLoggedWarning = 0L;
	
	protected Configuration hBaseConfiguration;
	protected Integer maxPerTableSize;
	protected ConcurrentHashMap<String,LinkedList<HTable>> hTablesByName;//cannot key by byte[] because .equals checks identity?
	protected Map<String,Class<PrimaryKey<?>>> primaryKeyClassByName;
	
	public HTablePool(Configuration hBaseConfiguration, Collection<String> names, 
			int minPerTableSize, int maxPerTableSize,
			Map<String,Class<PrimaryKey<?>>> primaryKeyClassByName){
		this.hBaseConfiguration = hBaseConfiguration;
		this.maxPerTableSize = maxPerTableSize;
		hTablesByName = new ConcurrentHashMap<String,LinkedList<HTable>>();
		for(String name : names){
			hTablesByName.put(name, new LinkedList<HTable>());
			for(int i=0; i < minPerTableSize; ++i){
				try{
					hTablesByName.get(name).add(
							new HTable(this.hBaseConfiguration, StringByteTool.getUtf8Bytes(name)));
				}catch(NoServerForRegionException nsfre){
					logger.error(ExceptionTool.getStackTraceAsString(nsfre));
				}catch(IOException e){
					throw new RuntimeException(e);
				}
			}
		}
		this.primaryKeyClassByName = primaryKeyClassByName;
	}
	
	
	public HTable checkOut(String name){
		DRCounters.inc("connection getHTable "+name);
		LinkedList<HTable> queue = hTablesByName.get(name);
		HTable hTable;
		synchronized(queue){
			hTable = queue.poll();
		}
		if(hTable==null){
			try{
				String counterName = "connection create HTable "+name;
				hTable = new HTable(this.hBaseConfiguration, name);
				logger.warn(counterName+", size="+queue.size());
				DRCounters.inc(counterName);
			}catch(IOException ioe){
				throw new RuntimeException(ioe);
			}
		}
		hTable.getWriteBuffer().clear();
		hTable.setAutoFlush(false);
		return hTable;
	}
	
	
	public void checkIn(HTable hTable){
		hTable.getWriteBuffer().clear();
		String name = StringByteTool.fromUtf8Bytes(hTable.getTableName());
		LinkedList<HTable> queue = hTablesByName.get(name);
		synchronized(queue){
			if(queue.size() < maxPerTableSize){
				queue.add(hTable);
			}else{
				try {
					logger.warn("checkIn HTable but queue already full, so close and discard, table="+name);
					hTable.close();
				} catch (IOException e) {
					logger.warn(ExceptionTool.getStackTraceAsString(e));
				}				
			}
		}
	}
	
//	public void killOutstandingConnections(){
//		for(String tableName : MapTool.nullSafe(hTablesByName).keySet()){
//			Collection<HTable> hTables = CollectionTool.nullSafe(hTablesByName.get(tableName));
//			for(HTable hTable : hTables){
////				hTable.close();//flushes buffer, which will probably block indefinitely in this situation
//				HConnection connection = hTable.getConnection();
//				logger.warn("aborting HConnection");
//				connection.abort("scuttling datarouter client", new DataAccessException());
//			}
//		}
//	}
	
	public Class<PrimaryKey<?>> getPrimaryKeyClass(String tableName){
		return primaryKeyClassByName.get(tableName);
	}
	
	public Integer getTotalPoolSize(){
		int totalPoolSize = 0;
		for(String tableName : MapTool.nullSafe(hTablesByName).keySet()){
			List<HTable> hTables = hTablesByName.get(tableName);
			totalPoolSize += CollectionTool.size(hTables);
		}
		return totalPoolSize;
	}
}
