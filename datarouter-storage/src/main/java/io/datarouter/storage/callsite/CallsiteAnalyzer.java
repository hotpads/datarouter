/**
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
package io.datarouter.storage.callsite;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.ComparableTool;
import io.datarouter.util.io.ReaderTool;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.string.StringTool;

public class CallsiteAnalyzer implements Callable<String>{
	private static final Logger logger = LoggerFactory.getLogger(CallsiteAnalyzer.class);

	private final String logPath;
	private final Integer maxResults;
	private final CallsiteStatComparator comparatorEnum;

	private Map<CallsiteStatKey,CallsiteStat> aggregateStatByKey = new HashMap<>();

	public CallsiteAnalyzer(String logPath, Integer maxResults, CallsiteStatComparator comparatorEnum){
		this.logPath = logPath;
		this.maxResults = maxResults;
		this.comparatorEnum = comparatorEnum;
	}

	@Override
	public String call(){
		//aggregate
		int numLines = 0;
		Date firstDate = new Date(Long.MAX_VALUE);
		Date lastDate = new Date(0);
		for(String line : ReaderTool.scanFileLines(logPath)){
			++numLines;
			CallsiteRecord record = CallsiteRecord.fromLogLine(line);
			if(ComparableTool.lt(record.getTimestamp(), firstDate)){
				firstDate = record.getTimestamp();
			}
			if(ComparableTool.gt(record.getTimestamp(), lastDate)){
				lastDate = record.getTimestamp();
			}
			CallsiteStat stat = new CallsiteStat(record.getCallsite(), record.getNodeName(),
					record.getDatarouterMethodName(), 1L, record.getDurationNs(), record.getNumItems());
			if(!aggregateStatByKey.containsKey(stat.getKey())){
				aggregateStatByKey.put(stat.getKey(), stat);
			}
			aggregateStatByKey.get(stat.getKey()).addMetrics(stat);
			if(numLines % 100000 == 0){
				logger.warn("scanned " + NumberFormatter.addCommas(numLines) + " in " + logPath);
			}
		}

		//sort
		List<CallsiteStat> stats = new ArrayList<>(aggregateStatByKey.values());
		Collections.sort(stats, Collections.reverseOrder(comparatorEnum.getComparator()));

		//build report
		CallsiteStatReportMetadata reportMetadata = CallsiteStatReportMetadata.inspect(stats);
		StringBuilder sb = new StringBuilder();
		int numDaoCallsites = CallsiteStat.countDaoCallsites(stats);
		long numSeconds = (lastDate.getTime() - firstDate.getTime()) / 1000;
		double callsPerSec = (double)numLines / numSeconds;
		sb.append("          path: " + logPath + "\n");
		sb.append(" file size (B): " + NumberFormatter.addCommas(new File(logPath).length()) + "\n");
		sb.append("         lines: " + NumberFormatter.addCommas(numLines) + "\n");
		sb.append("     callsites: " + NumberFormatter.addCommas(stats.size()) + "\n");
		sb.append(" dao callsites: " + NumberFormatter.addCommas(numDaoCallsites) + "\n");
		sb.append("    first date: " + firstDate.toString() + "\n");
		sb.append("     last date: " + lastDate.toString() + "\n");
		sb.append("       seconds: " + NumberFormatter.addCommas(numSeconds) + "\n");
		sb.append("     calls/sec: " + NumberFormatter.format(callsPerSec, 2) + "\n");
		sb.append("\n");
		int rankWidth = 5;
		sb.append(StringTool.pad("rank", ' ', rankWidth) + CallsiteStat.getReportHeader(reportMetadata) + "\n");
		//print top N
		int row = 0;
		for(CallsiteStat stat : stats){
			++row;
			if(row > maxResults){
				break;
			}
			sb.append(StringTool.pad(row + "", ' ', rankWidth) + stat.getReportLine(reportMetadata) + "\n");
		}
		return sb.toString();
	}

}
