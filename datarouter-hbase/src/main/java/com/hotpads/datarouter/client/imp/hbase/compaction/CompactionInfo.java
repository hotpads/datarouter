package com.hotpads.datarouter.client.imp.hbase.compaction;

import com.hotpads.datarouter.client.imp.hbase.cluster.DrRegionInfo;

public interface CompactionInfo{

	long getCompactionTriggerPeriodMs();
	long getPeriodMs(DrRegionInfo<?> regionInfo);

}