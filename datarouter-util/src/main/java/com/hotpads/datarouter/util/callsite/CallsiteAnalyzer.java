package com.hotpads.datarouter.util.callsite;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.util.core.DrComparableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrNumberFormatter;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.io.ReaderTool;
import com.hotpads.util.core.iterable.scanner.Scanner;

public class CallsiteAnalyzer implements Callable<String>{
	private static final Logger logger = LoggerFactory.getLogger(CallsiteAnalyzer.class);

	/****************** fields *********************/

	private final String logPath;
	private final Integer maxResults;
	private final CallsiteStatComparator comparatorEnum;

	private Map<CallsiteStatKeyX,CallsiteStatX> aggregateStatByKey = new HashMap<>();


	/****************** construct ********************/

	public CallsiteAnalyzer(String logPath, Integer maxResults, CallsiteStatComparator comparatorEnum){
		this.logPath = logPath;
		this.maxResults = maxResults;
		this.comparatorEnum = comparatorEnum;
	}

	@Override
	public String call(){
		//aggregate
		Scanner<List<String>> scanner = ReaderTool.scanFileLinesInBatches(logPath, 1000);
		int numLines = 0;
		Date firstDate = new Date(Long.MAX_VALUE);
		Date lastDate = new Date(0);
		while(scanner.advance()){
			List<String> batch = scanner.getCurrent();
			for(String line : batch){
				++numLines;
				CallsiteRecord record = CallsiteRecord.fromLogLine(line);
				if(DrComparableTool.lt(record.getTimestamp(), firstDate)){
					firstDate = record.getTimestamp();
				}
				if(DrComparableTool.gt(record.getTimestamp(), lastDate)){
					lastDate = record.getTimestamp();
				}
				CallsiteStatX stat = new CallsiteStatX(record.getCallsite(), record.getNodeName(), record
						.getDatarouterMethodName(), 1L, record.getDurationNs(), record.getNumItems());
				if(!aggregateStatByKey.containsKey(stat.getKey())){
					aggregateStatByKey.put(stat.getKey(), stat);
				}
				aggregateStatByKey.get(stat.getKey()).addMetrics(stat);
				if(numLines % 100000 == 0){
					logger.warn("scanned "+DrNumberFormatter.addCommas(numLines)+" in "+logPath);
				}
			}
		}

		//sort
		List<CallsiteStatX> stats = DrListTool.createArrayList(aggregateStatByKey.values());
		Collections.sort(stats, Collections.reverseOrder(comparatorEnum.getComparator()));

		//build report
		CallsiteStatReportMetadata reportMetadata = CallsiteStatReportMetadata.inspect(stats);
		StringBuilder sb = new StringBuilder();
		int numDaoCallsites = CallsiteStatX.countDaoCallsites(stats);
		long numSeconds = (lastDate.getTime() - firstDate.getTime()) / 1000;
		double callsPerSec = (double)numLines / (double)numSeconds;
		sb.append("          path: "+logPath+"\n");
		sb.append(" file size (B): "+DrNumberFormatter.addCommas(new File(logPath).length())+"\n");
		sb.append("         lines: "+DrNumberFormatter.addCommas(numLines)+"\n");
		sb.append("     callsites: "+DrNumberFormatter.addCommas(stats.size())+"\n");
		sb.append(" dao callsites: "+DrNumberFormatter.addCommas(numDaoCallsites)+"\n");
		sb.append("    first date: "+firstDate.toString()+"\n");
		sb.append("     last date: "+lastDate.toString()+"\n");
		sb.append("       seconds: "+DrNumberFormatter.addCommas(numSeconds)+"\n");
		sb.append("     calls/sec: "+DrNumberFormatter.format(callsPerSec, 2)+"\n");
		sb.append("\n");
		int rankWidth = 5;
		sb.append(DrStringTool.pad("rank", ' ', rankWidth) + CallsiteStatX.getReportHeader(reportMetadata)+"\n");
		//print top N
		int row = 0;
		for(CallsiteStatX stat : stats){
			++row;
			if(row > maxResults){
				break;
			}
			sb.append(DrStringTool.pad(row+"", ' ', rankWidth) + stat.getReportLine(reportMetadata) + "\n");
		}
		return sb.toString();
	}

}
