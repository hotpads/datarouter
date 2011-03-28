package com.hotpads.datarouter.client.imp.hbase.util;

import com.hotpads.datarouter.client.imp.hbase.DRHRegionInfo;

public interface CompactionInfo{

	Long getCompactionTriggerPeriodMs();
	Long getPeriodMinutes(DRHRegionInfo regionInfo);
	
}
