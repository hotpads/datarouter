package com.hotpads.datarouter.client.imp.hbase.balancer;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import org.apache.hadoop.hbase.ServerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.hbase.cluster.DrRegionInfo;
import com.hotpads.datarouter.client.imp.hbase.cluster.DrServerList;
import com.hotpads.datarouter.client.imp.hbase.cluster.DrRegionList;
import com.hotpads.datarouter.storage.key.entity.EntityPartitioner;
import com.hotpads.datarouter.storage.prefix.ScatteringPrefix;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.java.ReflectionTool;

public abstract class BaseHBaseRegionBalancer
implements Callable<Map<DrRegionInfo<?>,ServerName>>{
	private static final Logger logger = LoggerFactory.getLogger(BaseHBaseRegionBalancer.class);

	protected final String tableName;
	protected DrServerList drhServerList;
	protected DrRegionList drhRegionList;

	protected SortedMap<DrRegionInfo<?>,ServerName> serverByRegion;//fill and return this in the call() method
	protected Class<? extends ScatteringPrefix> scatteringPrefixClass;
	protected EntityPartitioner<?> entityPartitioner;
	protected ScatteringPrefix scatteringPrefix;
	
	protected BaseHBaseRegionBalancer(String tableName){
		this.tableName = tableName;
	}
	
	public BaseHBaseRegionBalancer init(Class<? extends ScatteringPrefix> scatteringPrefixClass,
			EntityPartitioner<?> entityPartitioner,
			DrServerList drhServerList, DrRegionList drhRegionList){
		// passed-in
		this.scatteringPrefixClass = scatteringPrefixClass;
		this.entityPartitioner = entityPartitioner;
		this.drhServerList = drhServerList;
		this.drhRegionList = drhRegionList;
		// internal
		this.serverByRegion = new TreeMap<>();
		if(scatteringPrefixClass != null){
			this.scatteringPrefix = ReflectionTool.create(scatteringPrefixClass);
		}
		return this;
	}

	public void assertRegionCountsConsistent(){
		if(drhRegionList.getRegions().size() != serverByRegion.size()){
			logger.error("regions:"+drhRegionList.getRegions());
			logger.error("balanced regions:"+serverByRegion.keySet());
			throw new RuntimeException("region count mismatch: input="+drhRegionList.getRegions().size()
					+", output="+serverByRegion.size());
		}
	}
	
	protected String getServerByRegionStringForDebug(){
		int i = 0;
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<DrRegionInfo<?>,ServerName> entry : serverByRegion.entrySet()){
			sb.append("\n"
					+DrStringTool.pad(i+"", ' ', 3)
					+" "+entry.getKey().getRegion().getEncodedName()
					+", "+entry.getValue());
			++i;
		}
		return sb.toString();
	}
}
