package com.hotpads.datarouter.client.imp.hbase.compaction.imp;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import javax.inject.Singleton;

import com.hotpads.datarouter.client.imp.hbase.cluster.DrRegionInfo;
import com.hotpads.datarouter.client.imp.hbase.compaction.CompactionInfo;

@Singleton
public class DefaultCompactionInfo implements CompactionInfo{

	private static final long DEFAULT_TRIGGER_PERIOD_MS = Duration.ofMinutes(10L).toMillis();
	private static final long DEFAULT_PERIOD_MS = ChronoUnit.WEEKS.getDuration().toMillis();

	@Override
	public long getCompactionTriggerPeriodMs(){
		return DEFAULT_TRIGGER_PERIOD_MS;
	}

	@Override
	public long getPeriodMs(DrRegionInfo<?> regionInfo){
		return DEFAULT_PERIOD_MS;
	}

}
