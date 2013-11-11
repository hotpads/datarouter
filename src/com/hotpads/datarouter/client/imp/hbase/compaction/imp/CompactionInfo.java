package com.hotpads.datarouter.client.imp.hbase.compaction.imp;

import com.hotpads.datarouter.client.imp.hbase.cluster.DRHRegionInfo;
import com.hotpads.datarouter.client.imp.hbase.compaction.DRHCompactionInfo;

public class CompactionInfo implements DRHCompactionInfo{

	@Override
	public Long getCompactionTriggerPeriodMs(){
		return null;
	}

	@Override
	public Long getPeriodMinutes(DRHRegionInfo regionInfo){
		return null;
	}

}
