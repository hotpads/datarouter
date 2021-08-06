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
package io.datarouter.client.hbase.balancer.imp;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.hadoop.hbase.ServerName;

import io.datarouter.client.hbase.balancer.BaseHBaseRegionBalancer;
import io.datarouter.client.hbase.cluster.DrRegionInfo;
import io.datarouter.util.StreamTool;

/**
 * Return the current server for each region.
 */
public class NoOpBalancer extends BaseHBaseRegionBalancer{

	public NoOpBalancer(String tableName){
		super(tableName);
	}

	@Override
	public Map<DrRegionInfo<?>,ServerName> call(){
		this.serverByRegion = drhRegionList.getRegions().stream()
				.collect(Collectors.toMap(Function.identity(), DrRegionInfo::getHBaseServerName, StreamTool
						.throwingMerger(), TreeMap::new));
		return serverByRegion;
	}

}
