package com.hotpads.datarouter.client.imp.hbase;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.NoServerForRegionException;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.profile.count.collection.Counters;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.bytes.StringByteTool;

public class HTablePool{
	protected Logger logger = Logger.getLogger(getClass());
	
	public static final Integer NUM_HTABLES_PER_TABLE_TO_STORE = 100;
	protected Long lastLoggedWarning = 0L;
	
	protected Configuration hBaseConfiguration;
	protected ConcurrentHashMap<String,LinkedList<HTable>> tablesByName;//cannot key by byte[] because .equals checks identity?
	protected Map<String,Class<PrimaryKey<?>>> primaryKeyClassByName;
	
	public HTablePool(Configuration hBaseConfiguration, Collection<String> names, int startingSize,
			Map<String,Class<PrimaryKey<?>>> primaryKeyClassByName){
		this.hBaseConfiguration = hBaseConfiguration;
		tablesByName = new ConcurrentHashMap<String,LinkedList<HTable>>();
		for(String name : names){
			tablesByName.put(name, new LinkedList<HTable>());
			for(int i=0; i < startingSize; ++i){
				try{
					tablesByName.get(name).add(
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
		Counters.inc("connection getHTable "+name);
		LinkedList<HTable> queue = tablesByName.get(name);
		HTable hTable;
		synchronized(queue){
			hTable = queue.poll();
		}
		if(hTable==null){
			try{
				hTable = new HTable(this.hBaseConfiguration, name);
				Counters.inc("connection create HTable "+name);
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
		LinkedList<HTable> queue = tablesByName.get(name);
		synchronized(queue){
			if(queue.size() < NUM_HTABLES_PER_TABLE_TO_STORE){
				queue.add(hTable);
			}
		}
	}
	
	public Class<PrimaryKey<?>> getPrimaryKeyClass(String tableName){
		return primaryKeyClassByName.get(tableName);
	}
}
