package com.hotpads.datarouter.client.imp.hbase.cluster;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.ClusterStatus;
import org.apache.hadoop.hbase.HServerLoad;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.hbase.HBaseStaticContext;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHServerInfo.DrhServerInfoHigherLoadComparator;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;

public class DRHServerList{
	
	private static final Logger logger = LoggerFactory.getLogger(DRHServerList.class);
	
	private final List<DRHServerInfo> servers;
	private final List<ServerName> serverNames;
	private final Map<ServerName,DRHServerInfo> drhServerInfoByServerName;
	private final SortedSet<DRHServerInfo> serversSortedByDescendingLoad;
	
	
	public DRHServerList(Configuration config){
		try{
			HBaseAdmin admin = HBaseStaticContext.ADMIN_BY_CONFIG.get(config);
			ClusterStatus clusterStatus = admin.getClusterStatus();
			serverNames = DrListTool.createArrayList(clusterStatus.getServers());
			Collections.sort(serverNames);
			this.servers = DrListTool.createArrayListWithSize(serverNames);
			this.serversSortedByDescendingLoad = new TreeSet<>(new DrhServerInfoHigherLoadComparator());
			this.drhServerInfoByServerName = new TreeMap<>();
			for(ServerName serverName : DrIterableTool.nullSafe(serverNames)){
				DRHServerInfo info = new DRHServerInfo(serverName, clusterStatus.getLoad(serverName));
				this.servers.add(info);
				this.serversSortedByDescendingLoad.add(info);
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
		SortedSet<String> serverNames = new TreeSet<>();
		for(DRHServerInfo server : servers){
			serverNames.add(server.getName());
		}
		return serverNames;
	}

	public SortedSet<String> getServerHostnames(){
		SortedSet<String> serverNames = new TreeSet<>();
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
	
	public int getNumServers(){
		return servers.size();
	}

	public List<DRHServerInfo> getServers(){
		return servers;
	}
	
	public SortedSet<DRHServerInfo> getServersSortedByDescendingLoad(){
		return serversSortedByDescendingLoad;
	}
}
