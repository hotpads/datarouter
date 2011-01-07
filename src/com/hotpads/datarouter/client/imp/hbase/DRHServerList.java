package com.hotpads.datarouter.client.imp.hbase;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.ClusterStatus;
import org.apache.hadoop.hbase.HServerAddress;
import org.apache.hadoop.hbase.HServerInfo;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.SetTool;

public class DRHServerList{
	Logger logger = Logger.getLogger(DRHServerList.class);

	protected List<DRHServerInfo> servers;
	protected Map<HServerAddress,HServerInfo> hServerInfoByHServerAddress;
	
	public DRHServerList(Configuration config){
		
		try{
			HBaseAdmin admin = new HBaseAdmin(config);
			ClusterStatus clusterStatus = admin.getClusterStatus();
			Collection<HServerInfo> hServers = clusterStatus.getServerInfo();
			this.servers = ListTool.createArrayListWithSize(hServers);
			this.hServerInfoByHServerAddress = MapTool.createTreeMap();
			for(HServerInfo hServer : hServers){
				this.servers.add(new DRHServerInfo(hServer));
				this.hServerInfoByHServerAddress.put(hServer.getServerAddress(), hServer);
			}
		}catch(IOException e){
			throw new DataAccessException(e);
		}
	}
	
	public SortedSet<String> getServerNames(){
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
	
	public HServerInfo getHServerInfo(HServerAddress server){
		return hServerInfoByHServerAddress.get(server);
	}

	public List<DRHServerInfo> getServers(){
		return servers;
	}
}
