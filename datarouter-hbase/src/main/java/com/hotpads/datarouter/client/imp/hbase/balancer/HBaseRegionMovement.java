package com.hotpads.datarouter.client.imp.hbase.balancer;

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
	
	public HBaseRegionMovement(String tableName, String regionName, ServerName currentServer,
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
			movementsByCurrentServer.putIfAbsent(movement.currentServer, new ArrayList<>()).add(movement);
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
		return "HBaseRegionMovement [tableName=" + tableName + ", regionName=" + regionName + ", currentServer="
				+ currentServer + ", destinationServer=" + destinationServer + "]";
	}
	
	
	
}
