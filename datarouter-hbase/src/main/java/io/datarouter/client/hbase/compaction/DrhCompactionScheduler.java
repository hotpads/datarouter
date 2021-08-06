/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.client.hbase.compaction;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import io.datarouter.client.hbase.cluster.DrRegionInfo;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.util.HashMethods;

public class DrhCompactionScheduler<PK extends PrimaryKey<PK>>{

	private static final long COMPACTION_EPOCH = LocalDate.parse("2011-03-01")
			.atStartOfDay(ZoneId.of("US/Eastern"))
			.toInstant()
			.toEpochMilli();

	private Long windowStartMs, windowEndMs;//start inclusive, end exclusive
	private DrRegionInfo<PK> regionInfo;
	private Long nextCompactTimeMs;

	public DrhCompactionScheduler(HBaseCompactionInfo compactionInfo, DrRegionInfo<PK> regionInfo){
		long now = System.currentTimeMillis();
		this.windowStartMs = now - now % compactionInfo.getCompactionTriggerPeriod().toMillis();
		this.windowEndMs = windowStartMs + compactionInfo.getCompactionTriggerPeriod().toMillis();
		this.regionInfo = Objects.requireNonNull(regionInfo);
		computeAndSetNextCompactTime(compactionInfo);
	}

	public boolean shouldCompact(){
		return nextCompactTimeMs >= windowStartMs && nextCompactTimeMs < windowEndMs;
	}

	private void computeAndSetNextCompactTime(HBaseCompactionInfo compactionInfo){
		//find the current period
		long regionCompactionPeriodMs = compactionInfo.getCompactionPeriod(regionInfo).toMillis();
		//careful, the division includes a floor because we're dealing with integers
		long periodStartSeekerMs = COMPACTION_EPOCH
				+ regionCompactionPeriodMs * ((windowStartMs - COMPACTION_EPOCH) / regionCompactionPeriodMs);

		String startKeyString = regionInfo.getRegion().getEncodedName();
		long regionHash = Math.abs(HashMethods.longDjbHash(startKeyString));

		//calculate an offset into the current period
		Double offsetIntoCompactionPeriodPct = 1d * regionHash / Long.MAX_VALUE;
		Long offsetIntoCompactionPeriodMs = (long)(offsetIntoCompactionPeriodPct * regionCompactionPeriodMs);
		nextCompactTimeMs = periodStartSeekerMs + offsetIntoCompactionPeriodMs;
		if(nextCompactTimeMs < windowStartMs){
			nextCompactTimeMs += regionCompactionPeriodMs;
		}
		nextCompactTimeMs = nextCompactTimeMs - nextCompactTimeMs % compactionInfo.getCompactionTriggerPeriod()
				.toMillis();
	}

	//used in hbaseTableRegions.jsp
	public String getNextCompactTimeFormatted(){
		ZonedDateTime date = Instant.ofEpochMilli(nextCompactTimeMs).atZone(ZoneId.systemDefault());
		return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm, EEE").format(date);
	}

}
