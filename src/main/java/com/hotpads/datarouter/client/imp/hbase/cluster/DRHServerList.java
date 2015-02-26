package com.hotpads.datarouter.client.imp.hbase.cluster;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.ClusterStatus;
import org.apache.hadoop.hbase.HServerLoad;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.hbase.factory.HBaseSimpleClientFactory;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.util.core.IterableTool;
import com.hotpads.datarouter.util.core.ListTool;
import com.hotpads.datarouter.util.core.MapTool;
import com.hotpads.datarouter.util.core.SetTool;

public class DRHServerList{
	Logger logger = LoggerFactory.getLogger(DRHServerList.class);

	protected List<DRHServerInfo> servers;
	protected List<ServerName> serverNames;
	protected Map<ServerName,DRHServerInfo> drhServerInfoByServerName;
	
	
	public DRHServerList(Configuration config){
		try{
			HBaseAdmin admin = HBaseSimpleClientFactory.ADMIN_BY_CONFIG.get(config);
			ClusterStatus clusterStatus = admin.getClusterStatus();
			serverNames = ListTool.createArrayList(clusterStatus.getServers());
			Collections.sort(serverNames);
			this.servers = ListTool.createArrayListWithSize(serverNames);
			this.drhServerInfoByServerName = MapTool.createTreeMap();
			for(ServerName serverName : IterableTool.nullSafe(serverNames)){
				DRHServerInfo info = new DRHServerInfo(serverName, clusterStatus.getLoad(serverName));
				this.servers.add(info);
				this.drhServerInfoByServerName.put(serverName, info);
			}
		}catch(IOException e){
			throw new DataAccessException(e);
		}
	}
	
	public List<ServerName> getServerNames(){
		return serverNames;
	}

	
	public List<ServerName> getServerNamesSorted(){
		return serverNames;
	}
	
	public SortedSet<String> getServerNameStrings(){
		SortedSet<String> serverNames = SetTool.createTreeSet();
		for(DRHServerInfo server : servers){
			serverNames.add(server.getName());
		}
		return serverNames;
	}

	public SortedSet<String> getServerHostnames(){
		SortedSet<String> serverNames = SetTool.createTreeSet();
		for(DRHServerInfo server : servers){
			serverNames.add(server.getHostname());
		}
		return serverNames;
	}
	
	public HServerLoad getHServerLoad(ServerName serverName){
		DRHServerInfo drhServerInfo = drhServerInfoByServerName.get(serverName);
		if(drhServerInfo==null){ 
			logger.warn("unexpected DRHServerInfo null for "+serverName.getHostAndPort());
			return null; 
		}
		return drhServerInfo.gethServerLoad();
	}

	public List<DRHServerInfo> getServers(){
		return servers;
	}
}
