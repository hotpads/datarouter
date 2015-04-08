package com.hotpads.datarouter.client.imp.hbase.balancer;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import org.apache.hadoop.hbase.ServerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.hbase.cluster.DRHRegionInfo;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHRegionList;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHServerList;
import com.hotpads.datarouter.storage.key.entity.EntityPartitioner;
import com.hotpads.datarouter.storage.prefix.ScatteringPrefix;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.java.ReflectionTool;

public abstract class BaseHBaseRegionBalancer
implements Callable<Map<DRHRegionInfo<?>,ServerName>>{
	
	private static final Logger logger = LoggerFactory.getLogger(BaseHBaseRegionBalancer.class);
	
	protected DRHServerList drhServerList;
	protected DRHRegionList drhRegionList;

	protected SortedMap<DRHRegionInfo<?>,ServerName> serverByRegion;//fill and return this in the call() method
	protected Class<? extends ScatteringPrefix> scatteringPrefixClass;
	protected EntityPartitioner<?> entityPartitioner;
	protected ScatteringPrefix scatteringPrefix;
	
	
	public BaseHBaseRegionBalancer init(Class<? extends ScatteringPrefix> scatteringPrefixClass,
			EntityPartitioner<?> entityPartitioner,
			DRHServerList drhServerList, DRHRegionList drhRegionList){
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
//		logger.warn("init complete, scatteringPrefixClass:"+scatteringPrefixClass);
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
		for(Map.Entry<DRHRegionInfo<?>,ServerName> entry : serverByRegion.entrySet()){
			sb.append("\n"
					+DrStringTool.pad(i+"", ' ', 3)
					+" "+entry.getKey().getRegion().getEncodedName()
					+", "+entry.getValue());
			++i;
		}
		return sb.toString();
	}
}
