package com.hotpads.datarouter.client.imp.hbase.util;

import java.util.Date;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.imp.hbase.DRHRegionInfo;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.DateTool;
import com.hotpads.util.core.HashMethods;
import com.hotpads.util.core.date.DailyCalendarTool;

public class CompactionScheduler
implements CompactionInfo{
	static Logger logger = Logger.getLogger(CompactionScheduler.class);
	
	public static final Long COMPACTION_EPOCH = DailyCalendarTool.parseYYYYMMDD("20110301").getTimeInMillis();
	
	protected Long now = System.currentTimeMillis();
	protected CompactionInfo compactionInfo;
	protected Long windowStartMs, windowEndMs;//start inclusive, end exclusive
	protected DRHRegionInfo regionInfo;
	protected Long regionHash;
	protected Long nextCompactTimeMs;
	
	public CompactionScheduler(CompactionInfo compactionInfo, DRHRegionInfo regionInfo){
		this.compactionInfo = compactionInfo;
		this.windowStartMs = now - (now % compactionInfo.getCompactionTriggerPeriodMs());
		this.windowEndMs = windowStartMs + compactionInfo.getCompactionTriggerPeriodMs();
		this.regionInfo = regionInfo;
		PrimaryKey<?> startKey = regionInfo.getStartKey();
		String startKeyString = startKey.getPersistentString();
		this.regionHash = Math.abs(HashMethods.longDJBHash(startKeyString));
		calculateNextCompactTime();
	}
	
	public String getNextCompactTimeFormatted(){
		Date date = new Date(nextCompactTimeMs);
		return DateTool.getYYYYMMDDHHMMWithPunctuation(date) + ", "
				+ DateTool.getDayAbbreviation(date);
	}
	
	public Long getNextCompactTimeMs(){
		return nextCompactTimeMs;
	}

	public boolean shouldCompact(){
		boolean inCurrentWindow = nextCompactTimeMs >= windowStartMs
				&& nextCompactTimeMs < windowEndMs;
		if(!inCurrentWindow){
//			logger.warn("skipping compaction of "+regionInfo.getStartKey().getPersistentString());
//			logger.warn("windowStart:"+new Date(windowStartMs)+", windowEnd:"+new Date(windowEndMs)
//					+", nextCompactTime"+new Date(nextCompactTimeMs));
			return false;
		}
		boolean moreThanOneStoreFile = regionInfo.getLoad().getStorefiles() > 1;
		if(!moreThanOneStoreFile){
			logger.warn("skipping compaction of "+regionInfo.getStartKey().getPersistentString()+" because only one file");
			return false;
		}
		return true;
	}
	
	public void calculateNextCompactTime(){
		//find the current period
		long regionCompactionPeriodMs = getPeriodMs();
		long periodStartSeekerMs = COMPACTION_EPOCH;
		while(true){
			long nextPeriodStartMs = periodStartSeekerMs + regionCompactionPeriodMs;
			if(nextPeriodStartMs > now){ break; }
			periodStartSeekerMs = nextPeriodStartMs;
		}

		//calculate an offset into the current period
		Double offsetIntoCompactionPeriodPct = 1d * (double)regionHash / (double)Long.MAX_VALUE;
		Long offsetIntoCompactionPeriodMs = (long)(offsetIntoCompactionPeriodPct * getPeriodMs());
		nextCompactTimeMs = periodStartSeekerMs + offsetIntoCompactionPeriodMs;
		if(nextCompactTimeMs < now){ nextCompactTimeMs += regionCompactionPeriodMs; }
		nextCompactTimeMs = nextCompactTimeMs - (nextCompactTimeMs % getCompactionTriggerPeriodMs());
	}
	
	public Long getPeriodMs(){
		return compactionInfo.getPeriodMinutes(regionInfo) * 60 * 1000;
	}
	
	@Override
	public Long getCompactionTriggerPeriodMs(){
		return compactionInfo.getCompactionTriggerPeriodMs();
	}
	
	@Override
	public Long getPeriodMinutes(DRHRegionInfo regionInfo){
		return compactionInfo.getPeriodMinutes(regionInfo);
	}
}
