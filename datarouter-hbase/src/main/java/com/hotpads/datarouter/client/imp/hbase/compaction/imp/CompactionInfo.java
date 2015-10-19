package com.hotpads.datarouter.client.imp.hbase.compaction.imp;

import com.hotpads.datarouter.client.imp.hbase.cluster.DrRegionInfo;
import com.hotpads.datarouter.client.imp.hbase.compaction.DRHCompactionInfo;

public class CompactionInfo implements DRHCompactionInfo{

	public static final Long 
		RUN_EVERY_X_MINUTES = 10L,
		RUN_EVERY_X_MS = RUN_EVERY_X_MINUTES * 60 * 1000;
	
	@Override
	public Long getCompactionTriggerPeriodMs(){
		return RUN_EVERY_X_MS;
	}

	@Override
	public Long getPeriodMinutes(DrRegionInfo regionInfo){
		return RUN_EVERY_X_MINUTES;
	}

}
