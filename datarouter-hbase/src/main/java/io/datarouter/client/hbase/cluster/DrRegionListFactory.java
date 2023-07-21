/*
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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HRegionLocation;
import org.apache.hadoop.hbase.RegionLoad;
import org.apache.hadoop.hbase.ServerLoad;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.RegionLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.hbase.HBaseClientManager;
import io.datarouter.client.hbase.balancer.BaseHBaseRegionBalancer;
import io.datarouter.client.hbase.compaction.HBaseCompactionInfo;
import io.datarouter.client.hbase.util.HBaseClientTool;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.key.entity.EntityPartitioner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.serialize.fieldcache.EntityFieldInfo;
import io.datarouter.util.concurrent.CallableTool;
import jakarta.inject.Inject;

public class DrRegionListFactory{
	private static final Logger logger = LoggerFactory.getLogger(DrRegionList.class);

	@Inject
	private HBaseClientManager hBaseClientManager;
	@Inject
	private HBaseCompactionInfo compactionInfo;

	public DrRegionList make(ClientId clientId, DrServerList servers, String tableName, PhysicalNode<?,?,?> node,
			BaseHBaseRegionBalancer balancer){
		List<DrRegionInfo<?>> regions = new ArrayList<>();
		EntityFieldInfo<?,?> entityFieldInfo = HBaseClientTool.getEntityFieldInfo(node);
		EntityPartitioner<?> entityPartitioner = entityFieldInfo.getEntityPartitioner();

		Map<HRegionInfo,ServerName> serverNameByHRegionInfo = getServerNameByHRegionInfo(clientId, tableName);
		Map<String,RegionLoad> regionLoadByName = new TreeMap<>();
		for(DrServerInfo server : servers.getServers()){
			ServerLoad serverLoad = server.getServerLoad();
			Map<byte[],RegionLoad> regionsLoad = serverLoad.getRegionsLoad();
			for(RegionLoad regionLoad : regionsLoad.values()){
				String name = HRegionInfo.encodeRegionName(regionLoad.getName());
				regionLoadByName.put(name, regionLoad);
			}
		}
		int regionNum = 0;
		for(HRegionInfo hregionInfo : serverNameByHRegionInfo.keySet()){
			try{
				RegionLoad regionLoad = regionLoadByName.get(hregionInfo.getEncodedName());
				ServerName serverName = serverNameByHRegionInfo.get(hregionInfo);
				regions.add(new DrRegionInfo<>(regionNum++, tableName, hregionInfo, serverName, node, regionLoad,
						compactionInfo, entityFieldInfo));
			}catch(RuntimeException e){
				logger.warn("couldn't build DRHRegionList for region:" + hregionInfo.getEncodedName());
				throw e;
			}
		}
		Collections.sort(regions);// ensure sorted for getRegionsSorted
		DrRegionList drRegionList = new DrRegionList(regions);
		balancer.init(entityPartitioner, servers, drRegionList);
		logger.info("starting balancerStrategy for {} with {} regions", tableName, regions.size());
		Map<DrRegionInfo<?>,ServerName> targetServerNameByRegion = CallableTool.callUnchecked(balancer);
		for(DrRegionInfo<?> drhRegionInfo : regions){
			drhRegionInfo.setBalancerDestinationServer(targetServerNameByRegion.get(drhRegionInfo));
		}
		balancer.assertRegionCountsConsistent();
		return drRegionList;
	}

	private Map<HRegionInfo,ServerName> getServerNameByHRegionInfo(ClientId clientId, String tableName){
		try{
			Connection connection = hBaseClientManager.getConnection(clientId);
			RegionLocator regionLocator = connection.getRegionLocator(TableName.valueOf(tableName));
			Map<HRegionInfo,ServerName> serverNameByHRegionInfo = regionLocator.getAllRegionLocations().stream()
					.collect(Collectors.toMap(HRegionLocation::getRegionInfo, HRegionLocation::getServerName));
			return serverNameByHRegionInfo;
		}catch(IOException e){
			throw new DataAccessException(e);
		}
	}

	public static class DrRegionList{

		public static final String GROUP_BY_ALL = "all";

		private final List<DrRegionInfo<?>> regions;

		private DrRegionList(List<DrRegionInfo<?>> regions){
			this.regions = regions;
		}

		public List<DrRegionInfo<?>> getRegions(){
			return regions;
		}

		public DrRegionInfo<?> getRegionByEncodedName(String encodedName){
			for(DrRegionInfo<?> region : regions){
				if(region.getRegion().getEncodedName().equals(encodedName)){
					return region;
				}
			}
			return null;
		}

		public DrRegionInfo<?> getRegionAfter(String encodedName){
			boolean foundFirstRegion = false;
			for(DrRegionInfo<?> region : regions){
				if(foundFirstRegion){
					return region;
				}
				if(region.getRegion().getEncodedName().equals(encodedName)){
					foundFirstRegion = true;
				}
			}
			return null;
		}

		private SortedMap<String,List<DrRegionInfo<?>>> getRegionsByServerName(){
			SortedMap<String,List<DrRegionInfo<?>>> out = new TreeMap<>();
			for(DrRegionInfo<?> region : regions){
				String serverName = region.getServerName();
				if(out.get(serverName) == null){
					out.put(serverName, new LinkedList<>());
				}
				out.get(serverName).add(region);
			}
			return out;
		}

		public LinkedHashMap<String,List<DrRegionInfo<?>>> getRegionsGroupedBy(String groupBy){
			LinkedHashMap<String,List<DrRegionInfo<?>>> regionsByGroup = new LinkedHashMap<>();
			if(GROUP_BY_ALL.equals(groupBy)){
				regionsByGroup.put("all", regions);
			}else if("serverName".equals(groupBy)){
				for(Entry<String,List<DrRegionInfo<?>>> entry : getRegionsByServerName().entrySet()){
					regionsByGroup.put(entry.getKey(), entry.getValue());
				}
			}
			return regionsByGroup;
		}

	}

}
