package com.hotpads.datarouter.client.imp.hbase.cluster;

import org.apache.hadoop.hbase.HServerLoad;
import org.apache.hadoop.hbase.ServerName;

public class DRHServerInfo{
	
	protected ServerName serverName;
	protected HServerLoad hServerLoad;

	protected String name;
	protected String hostname;
	
	
	public DRHServerInfo(ServerName serverName, HServerLoad hServerLoad){
		this.serverName = serverName;
		this.hServerLoad = hServerLoad;
		this.name = serverName.getServerName();
		this.hostname = serverName.getHostname();
	}

	public String getName(){
		return name;
	}
	
	public String getHostname(){
		return hostname;
	}

	public ServerName getServerName() {
		return serverName;
	}

	public HServerLoad gethServerLoad() {
		return hServerLoad;
	}

	
}
