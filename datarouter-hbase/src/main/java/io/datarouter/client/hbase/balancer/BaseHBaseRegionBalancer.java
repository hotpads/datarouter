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
package io.datarouter.client.hbase.balancer;

import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import org.apache.hadoop.hbase.ServerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.hbase.cluster.DrRegionInfo;
import io.datarouter.client.hbase.cluster.DrRegionListFactory.DrRegionList;
import io.datarouter.client.hbase.cluster.DrServerList;
import io.datarouter.model.key.entity.EntityPartitioner;
import io.datarouter.util.string.StringTool;

public abstract class BaseHBaseRegionBalancer implements Callable<Map<DrRegionInfo<?>,ServerName>>{
	private static final Logger logger = LoggerFactory.getLogger(BaseHBaseRegionBalancer.class);

	protected final String tableName;
	protected DrServerList drhServerList;
	protected DrRegionList drhRegionList;

	protected SortedMap<DrRegionInfo<?>,ServerName> serverByRegion;//fill and return this in the call() method
	protected EntityPartitioner<?> entityPartitioner;

	protected BaseHBaseRegionBalancer(String tableName){
		this.tableName = tableName;
	}

	public BaseHBaseRegionBalancer init(
			EntityPartitioner<?> entityPartitioner,
			DrServerList drhServerList,
			DrRegionList drhRegionList){
		this.entityPartitioner = entityPartitioner;
		this.drhServerList = drhServerList;
		this.drhRegionList = drhRegionList;
		this.serverByRegion = new TreeMap<>();
		return this;
	}

	public void assertRegionCountsConsistent(){
		if(drhRegionList.getRegions().size() != serverByRegion.size()){
			logger.error("regions:" + drhRegionList.getRegions());
			logger.error("balanced regions:" + serverByRegion.keySet());
			throw new RuntimeException("region count mismatch: input=" + drhRegionList.getRegions().size() + ", output="
					+ serverByRegion.size());
		}
	}

	protected String getServerByRegionStringForDebug(){
		int idx = 0;
		StringBuilder sb = new StringBuilder();
		for(Entry<DrRegionInfo<?>,ServerName> entry : serverByRegion.entrySet()){
			sb.append("\n"
					+ StringTool.pad(idx + "", ' ', 3)
					+ " " + entry.getKey().getRegion().getEncodedName()
					+ ", " + entry.getValue());
			++idx;
		}
		return sb.toString();
	}

}
