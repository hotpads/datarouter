package com.hotpads.datarouter.client.imp.hbase.compaction;

import com.hotpads.datarouter.client.imp.hbase.cluster.DRHRegionInfo;

public interface DRHCompactionInfo{

	Long getCompactionTriggerPeriodMs();
	Long getPeriodMinutes(DRHRegionInfo regionInfo);
	
}
