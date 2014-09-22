package com.hotpads.datarouter.client.imp.hbase.task;

public class HBaseTaskNameParams{

	private String clientName;
	private String tableName;
	private String nodeName;
	
	
	public HBaseTaskNameParams(String clientName, String tableName, String nodeName){
		this.clientName = clientName;
		this.tableName = tableName;
		this.nodeName = nodeName;
	}


	public String getClientName(){
		return clientName;
	}

	public String getTableName(){
		return tableName;
	}

	public String getNodeName(){
		return nodeName;
	}
	
}
