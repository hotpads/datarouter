package com.hotpads.datarouter.client.imp.hbase.compaction;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.imp.hbase.cluster.DrRegionInfo;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrDailyCalendarTool;
import com.hotpads.datarouter.util.core.DrDateTool;
import com.hotpads.datarouter.util.core.DrHashMethods;

public class DrhCompactionScheduler<PK extends PrimaryKey<PK>>{
	private static final Logger logger = LoggerFactory.getLogger(DrhCompactionScheduler.class);

	private static final long COMPACTION_EPOCH = DrDailyCalendarTool.parseYYYYMMDDEastern("20110301").getTimeInMillis();

	private Long windowStartMs, windowEndMs;//start inclusive, end exclusive
	private DrRegionInfo<PK> regionInfo;
	private Long nextCompactTimeMs;

	public DrhCompactionScheduler(HBaseCompactionInfo compactionInfo, DrRegionInfo<PK> regionInfo){
		long now = System.currentTimeMillis();
		this.windowStartMs = now - now % compactionInfo.getCompactionTriggerPeriodMs();
		this.windowEndMs = windowStartMs + compactionInfo.getCompactionTriggerPeriodMs();
		this.regionInfo = Preconditions.checkNotNull(regionInfo);
		computeAndSetNextCompactTime(compactionInfo);
	}

	public boolean shouldCompact(){
		//tease out NPE's and return false if we hit one.  this happens occasionally for some reason
		if(regionInfo.getLoad()==null){
			logger.warn("regionInfo.getLoad()==null on "+regionInfo.getTableName()+" "+regionInfo.getName());
			return false;
		}

		boolean inCurrentWindow = nextCompactTimeMs >= windowStartMs
				&& nextCompactTimeMs < windowEndMs;
		if(!inCurrentWindow){
			return false;
		}

		boolean onlyOneStoreFile = regionInfo.getLoad().getStorefiles() <= 1;
		if(onlyOneStoreFile){//still need to compact to localize the hdfs blocks
			logger.warn("skipping compaction of " + regionInfo.getRegion().getEncodedName() + ", " + regionInfo
					.getStartKey().toString() + " because only one file");
			return false;
		}
		return true;
	}

	private void computeAndSetNextCompactTime(HBaseCompactionInfo compactionInfo){
		//find the current period
		long regionCompactionPeriodMs = compactionInfo.getPeriodMs(regionInfo);
		//careful, the division includes a floor because we're dealing with integers
		long periodStartSeekerMs = COMPACTION_EPOCH
				+ regionCompactionPeriodMs * ((windowStartMs - COMPACTION_EPOCH) / regionCompactionPeriodMs);

		String startKeyString = regionInfo.getRegion().getEncodedName();
		long regionHash = Math.abs(DrHashMethods.longDJBHash(startKeyString));

		//calculate an offset into the current period
		Double offsetIntoCompactionPeriodPct = 1d * regionHash / Long.MAX_VALUE;
		Long offsetIntoCompactionPeriodMs = (long)(offsetIntoCompactionPeriodPct * compactionInfo.getPeriodMs(
				regionInfo));
		nextCompactTimeMs = periodStartSeekerMs + offsetIntoCompactionPeriodMs;
		if(nextCompactTimeMs < windowStartMs){
			nextCompactTimeMs += regionCompactionPeriodMs;
		}
		nextCompactTimeMs = nextCompactTimeMs - nextCompactTimeMs % compactionInfo.getCompactionTriggerPeriodMs();
	}

	//used in hbaseTableRegions.jsp
	public String getNextCompactTimeFormatted(){
		Date date = new Date(nextCompactTimeMs);
		return DrDateTool.getYYYYMMDDHHMMWithPunctuation(date) + ", " + DrDateTool.getDayAbbreviation(date);
	}

}
