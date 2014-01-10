package com.hotpads.datarouter.client.imp.hbase.compaction;

import java.util.Date;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHRegionInfo;
import com.hotpads.util.core.DateTool;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.HashMethods;
import com.hotpads.util.core.date.DailyCalendarTool;

public class DRHCompactionScheduler
implements DRHCompactionInfo{
	static Logger logger = Logger.getLogger(DRHCompactionScheduler.class);
	
	public static final Long COMPACTION_EPOCH = 
		DailyCalendarTool.parseYYYYMMDDEastern("20110301").getTimeInMillis();
	
	protected Long now = System.currentTimeMillis();
	protected DRHCompactionInfo compactionInfo;
	protected Long windowStartMs, windowEndMs;//start inclusive, end exclusive
	protected DRHRegionInfo regionInfo;
	protected Long regionHash;
	protected Long nextCompactTimeMs;
	
	public DRHCompactionScheduler(DRHCompactionInfo compactionInfo, DRHRegionInfo regionInfo){
		this.compactionInfo = compactionInfo;
		this.windowStartMs = now - (now % compactionInfo.getCompactionTriggerPeriodMs());
		this.windowEndMs = windowStartMs + compactionInfo.getCompactionTriggerPeriodMs();
		this.regionInfo = Preconditions.checkNotNull(regionInfo);
		//these can apparently be null for some reason.  implementation should handle it
//		Preconditions.checkNotNull(regionInfo.getRegion(), regionInfo.getTableName()+" "+regionInfo.getName());
//		Preconditions.checkNotNull(regionInfo.getLoad().getStorefiles(), regionInfo.getTableName()+" "+regionInfo.getName());
		String startKeyString = regionInfo.getRegion().getEncodedName();
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
//			logger.warn("windowStart:"+DateTool.getYYYYMMDDHHMMSSMMMWithPunctuationNoSpaces(new Date(windowStartMs))
//					+", windowEnd:"+DateTool.getYYYYMMDDHHMMSSMMMWithPunctuationNoSpaces(new Date(windowEndMs))
//					+", nextCompactTime"+DateTool.getYYYYMMDDHHMMSSMMMWithPunctuationNoSpaces(new Date(nextCompactTimeMs)));
			return false;
		}
		
		//tease out NPE's and return false if we hit one.  this happens occasionally for some reason
		try{
			regionInfo.getLoad();
			regionInfo.getLoad().getStorefiles();
		}catch(NullPointerException npe){
			logger.warn(ExceptionTool.getStackTraceAsString(npe));
			return false; 
		}
		
		boolean moreThanOneStoreFile = regionInfo.getLoad().getStorefiles() > 1;
		if(!moreThanOneStoreFile){//still need to compact to localize the hdfs blocks
			logger.warn("compacting "+regionInfo.getRegion().getEncodedName()+", "
					+regionInfo.getStartKey().toString()+" even though only one store file");
//			logger.warn("skipping compaction of "+regionInfo.getRegion().getEncodedName()+", "
//					+regionInfo.getStartKey().toString()+" because only one file");
//			return false;			
		}
		return true;
	}
	
	public void calculateNextCompactTime(){
		//find the current period
		long regionCompactionPeriodMs = getPeriodMs();
		long periodStartSeekerMs = COMPACTION_EPOCH;
		while(true){
			long nextPeriodStartMs = periodStartSeekerMs + regionCompactionPeriodMs;
			if(nextPeriodStartMs > windowStartMs){ break; }
			periodStartSeekerMs = nextPeriodStartMs;
		}

		//calculate an offset into the current period
		Double offsetIntoCompactionPeriodPct = 1d * (double)regionHash / (double)Long.MAX_VALUE;
		Long offsetIntoCompactionPeriodMs = (long)(offsetIntoCompactionPeriodPct * getPeriodMs());
		nextCompactTimeMs = periodStartSeekerMs + offsetIntoCompactionPeriodMs;
		if(nextCompactTimeMs < windowStartMs){ nextCompactTimeMs += regionCompactionPeriodMs; }
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
