package com.hotpads.datarouter.client.imp.hbase;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;

public class HTablePool{
	
	protected HBaseConfiguration hBaseConfiguration;
	protected Map<byte[],Stack<HTable>> tablesByName;
	
	public HTablePool(HBaseConfiguration hBaseConfiguration, Collection<byte[]> names, int startingSize){
		this.hBaseConfiguration = hBaseConfiguration;
		tablesByName = new ConcurrentHashMap<byte[],Stack<HTable>>();
		for(byte[] name : names){
			tablesByName.put(name, new Stack<HTable>());
			for(int i=0; i < startingSize; ++i){
				try{
					tablesByName.get(name).add(new HTable(this.hBaseConfiguration, name));
				}catch(IOException e){
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	public HTable checkOut(byte[] name){
		HTable hTable = tablesByName.get(name).pop();
		if(hTable!=null){ return hTable; }
		try{
			return new HTable(this.hBaseConfiguration, name);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	
	public void checkIn(HTable hTable){
		tablesByName.get(hTable.getTableName()).push(hTable);
	}
	
}
