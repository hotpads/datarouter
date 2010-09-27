package com.hotpads.datarouter.client.imp.hbase;

import java.io.IOException;
import java.util.Collection;
import java.util.EmptyStackException;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.log4j.Logger;

import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.bytes.StringByteTool;

public class HTablePool{
	protected Logger logger = Logger.getLogger(getClass());
	
	public static final Integer MAX_HTABLES_PER_TABLE = 10;
	protected Long lastLoggedWarning = 0L;
	
	protected Configuration hBaseConfiguration;
	protected ConcurrentHashMap<String,Stack<HTable>> tablesByName;//cannot key by byte[] because .equals checks identity?
	
	public HTablePool(Configuration hBaseConfiguration, Collection<String> names, int startingSize){
		this.hBaseConfiguration = hBaseConfiguration;
		tablesByName = new ConcurrentHashMap<String,Stack<HTable>>();
		for(String name : names){
			tablesByName.put(name, new Stack<HTable>());
			for(int i=0; i < startingSize; ++i){
				try{
					tablesByName.get(name).add(
							new HTable(this.hBaseConfiguration, StringByteTool.getUtf8Bytes(name)));
				}catch(IOException e){
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	
	public HTable checkOut(String name){
		Stack<HTable> stack = tablesByName.get(name);
		try{
			HTable hTable = stack.pop();
			return hTable;
		}catch(EmptyStackException ese){
			try{
				return new HTable(this.hBaseConfiguration, name);
			}catch(IOException ioe){
				throw new RuntimeException(ioe);
			}
		}
	}
	
	
	public void checkIn(HTable hTable){
		String name = StringByteTool.fromUtf8Bytes(hTable.getTableName());
		tablesByName.get(name).push(hTable);
	}
	
	
	protected void logIfManyHTables(String name, Stack<HTable> stack){
		if(CollectionTool.size(stack) > MAX_HTABLES_PER_TABLE 
				&& System.currentTimeMillis() - lastLoggedWarning > 1000){
			logger.warn("hTables for "+name+"="+CollectionTool.size(stack));
			lastLoggedWarning = System.currentTimeMillis();
		}
	}
}
