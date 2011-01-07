package com.hotpads.datarouter.client.imp.hbase;

import org.apache.hadoop.hbase.HServerInfo;

public class DRHServerInfo{

	protected String name;
	protected String hostname;
	protected HServerInfo hServerInfo;
	
	
	public DRHServerInfo(HServerInfo hServerInfo){
		this.hServerInfo = hServerInfo;
		this.name = hServerInfo.getServerName();
		this.hostname = hServerInfo.getHostname();
	}

	public String getName(){
		return name;
	}
	
	public String getHostname(){
		return hostname;
	}

	public HServerInfo getHserverInfo(){
		return hServerInfo;
	}
	
}
