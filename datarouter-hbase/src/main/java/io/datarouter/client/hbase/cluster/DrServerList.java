/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.client.hbase.cluster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.hadoop.hbase.ClusterStatus;
import org.apache.hadoop.hbase.ServerLoad;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.client.Admin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.model.exception.DataAccessException;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.iterable.IterableTool;

public class DrServerList{
	private static final Logger logger = LoggerFactory.getLogger(DrServerList.class);

	private final List<DrServerInfo> servers;
	private final List<ServerName> serverNames;
	private final Map<ServerName,DrServerInfo> drhServerInfoByServerName;
	private final SortedSet<DrServerInfo> serversSortedByDescendingLoad;

	public DrServerList(Admin admin){
		try{
			ClusterStatus clusterStatus = admin.getClusterStatus();
			serverNames = new ArrayList<>(clusterStatus.getServers());
			Collections.sort(serverNames);
			this.servers = ListTool.createArrayListWithSize(serverNames);
			this.serversSortedByDescendingLoad = new TreeSet<>(DrServerInfo.COMPARATOR_DESC_SERVER_LOAD);
			this.drhServerInfoByServerName = new TreeMap<>();
			for(ServerName serverName : IterableTool.nullSafe(serverNames)){
				DrServerInfo info = new DrServerInfo(serverName, clusterStatus.getLoad(serverName));
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
		for(DrServerInfo server : servers){
			serverNames.add(server.getName());
		}
		return serverNames;
	}

	public SortedSet<String> getServerHostnames(){
		SortedSet<String> serverNames = new TreeSet<>();
		for(DrServerInfo server : servers){
			serverNames.add(server.getHostname());
		}
		return serverNames;
	}

	public ServerLoad getHServerLoad(ServerName serverName){
		DrServerInfo drhServerInfo = drhServerInfoByServerName.get(serverName);
		if(drhServerInfo == null){
			logger.warn("unexpected DRHServerInfo null for " + serverName.getHostAndPort());
			return null;
		}
		return drhServerInfo.getServerLoad();
	}

	public int getNumServers(){
		return servers.size();
	}

	public List<DrServerInfo> getServers(){
		return servers;
	}

	public SortedSet<DrServerInfo> getServersSortedByDescendingLoad(){
		return serversSortedByDescendingLoad;
	}

}
