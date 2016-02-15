package com.hotpads.datarouter.client.imp.hbase.compaction;

import com.hotpads.datarouter.client.imp.hbase.cluster.DrRegionInfo;

public interface HBaseCompactionInfo{

	long getCompactionTriggerPeriodMs();
	long getPeriodMs(DrRegionInfo<?> regionInfo);
	String getDisplayServerName(String serverName);

}