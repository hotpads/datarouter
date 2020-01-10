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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.util.Bytes;

public class HBaseRegionMovement{

	private final String tableName;
	private final String regionName;
	private final ServerName currentServer;
	private final ServerName destinationServer;

	public HBaseRegionMovement(
			String tableName,
			String regionName,
			ServerName currentServer,
			ServerName destinationServer){
		this.tableName = tableName;
		this.regionName = regionName;
		this.currentServer = currentServer;
		this.destinationServer = destinationServer;
	}

	public static Map<ServerName,List<HBaseRegionMovement>> getByCurrentServer(
			Collection<HBaseRegionMovement> movements){
		Map<ServerName,List<HBaseRegionMovement>> movementsByCurrentServer = new TreeMap<>();
		for(HBaseRegionMovement movement : movements){
			movementsByCurrentServer.putIfAbsent(movement.currentServer, new ArrayList<>());
			movementsByCurrentServer.get(movement.currentServer).add(movement);
		}
		return movementsByCurrentServer;
	}

	public byte[] getRegionNameBytes(){
		return Bytes.toBytes(regionName);
	}

	public byte[] getDestinationServerNameBytes(){
		return Bytes.toBytes(destinationServer.getServerName());
	}

	@Override
	public String toString(){
		return String.format(
				"HBaseRegionMovement [tableName=%s, regionName=%s, currentServer=%s, destinationServer=%s]", tableName,
				regionName, currentServer, destinationServer);
	}

}
