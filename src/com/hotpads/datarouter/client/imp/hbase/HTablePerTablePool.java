package com.hotpads.datarouter.client.imp.hbase;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.NoServerForRegionException;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.bytes.StringByteTool;

@Deprecated//use HTableExecutorServicePool which pools at a lower level for less waste
public class HTablePerTablePool implements HTablePool{
	protected Logger logger = Logger.getLogger(getClass());
	
	protected Long lastLoggedWarning = 0L;
	
	protected Configuration hBaseConfiguration;
	protected Integer maxPerTableSize;
	protected ConcurrentHashMap<String,LinkedList<HTable>> hTablesByName;//cannot key by byte[] because .equals only checks identity
	
	public HTablePerTablePool(Configuration hBaseConfiguration, Collection<String> names, 
			int minPerTableSize, int maxPerTableSize){
		this.hBaseConfiguration = hBaseConfiguration;
		this.maxPerTableSize = maxPerTableSize;
		hTablesByName = new ConcurrentHashMap<String,LinkedList<HTable>>();
		for(String name : names){
			hTablesByName.put(name, new LinkedList<HTable>());
			for(int i=0; i < minPerTableSize; ++i){
				try{
					hTablesByName.get(name).add(new HTable(this.hBaseConfiguration, 
							StringByteTool.getUtf8Bytes(name)));
				}catch(NoServerForRegionException nsfre){
					logger.error(ExceptionTool.getStackTraceAsString(nsfre));
				}catch(IOException e){
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	
	@Override
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
	
	
	@Override
	public void checkIn(HTable hTable, boolean possiblyTarnished){
		//TODO what if the hTable's ExecutorService is junked up?  maybe add a parameter about whether to discard it
		hTable.getWriteBuffer().clear();
		String name = StringByteTool.fromUtf8Bytes(hTable.getTableName());
		LinkedList<HTable> queue = hTablesByName.get(name);
		boolean addedBackToPool = false;
		if(possiblyTarnished){
			addedBackToPool = false;
			logger.warn("HTable possibly tarnished, discarding.  table:"+name);
			DRCounters.inc("HTable possibly tarnished "+name);	
		}
		synchronized(queue){
			if(queue.size() < maxPerTableSize){
				queue.add(hTable);
				addedBackToPool = true;
				DRCounters.inc("connection HTable returned to pool "+name);
			}
		}
		if(!addedBackToPool){
			try {
				logger.warn("checkIn HTable but queue already full or possibly tarnished, so close and discard, table="+name);
				hTable.close();//flushes write buffer, and calls ExecutorService.shutdown()
				DRCounters.inc("connection HTable closed "+name);
			} catch (IOException e) {
				logger.warn(ExceptionTool.getStackTraceAsString(e));
			}				
		}
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
