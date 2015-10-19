package com.hotpads.datarouter.client.imp.hbase.compaction;

import com.google.inject.ImplementedBy;
import com.hotpads.datarouter.client.imp.hbase.cluster.DrRegionInfo;
import com.hotpads.datarouter.client.imp.hbase.compaction.imp.CompactionInfo;

@ImplementedBy(CompactionInfo.class)
public interface DRHCompactionInfo{

	Long getCompactionTriggerPeriodMs();
	Long getPeriodMinutes(DrRegionInfo regionInfo);
	
}
