package com.hotpads.datarouter.client.imp.hbase.cluster;

import java.util.Comparator;

import org.apache.hadoop.hbase.ServerLoad;
import org.apache.hadoop.hbase.ServerName;

public class DrServerInfo{

	private final ServerName serverName;
	private final ServerLoad serverLoad;

	private final String name;
	private final String hostname;


	public DrServerInfo(ServerName serverName, ServerLoad serverLoad){
		this.serverName = serverName;
		this.serverLoad = serverLoad;
		this.name = serverName.getServerName();
		this.hostname = serverName.getHostname();
	}


	/************** comparator ******************/

	public static class DrhServerInfoHigherLoadComparator implements Comparator<DrServerInfo>{
		@Override
		public int compare(DrServerInfo serverA, DrServerInfo serverB){
			int numARegions = serverA.getServerLoad().getLoad();
			int numBRegions = serverB.getServerLoad().getLoad();
			return numBRegions - numARegions;
		}
	}


	/************** get/set ***************************/

	public String getName(){
		return name;
	}

	public String getHostname(){
		return hostname;
	}

	public ServerName getServerName(){
		return serverName;
	}

	public ServerLoad getServerLoad(){
		return serverLoad;
	}


}
