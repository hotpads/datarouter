package com.hotpads.datarouter.util.callsite;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.util.callsite.CallsiteStatX.CallsiteCountComparator;
import com.hotpads.datarouter.util.core.ComparableTool;
import com.hotpads.datarouter.util.core.ListTool;
import com.hotpads.datarouter.util.core.NumberFormatter;
import com.hotpads.datarouter.util.core.StringTool;
import com.hotpads.util.core.io.ReaderTool;
import com.hotpads.util.core.iterable.scanner.Scanner;

public class CallsiteAnalyzer implements Callable<String>{
	private static final Logger logger = LoggerFactory.getLogger(CallsiteAnalyzer.class);
	// scp -i /hpdev/ec2-latest/.EC2_Keys/.ec2key root@webtest2:/mnt/logs/callsite.log /tmp/callsite.log
	
//	private static final String LOG_LOCATION = "/mnt/hdd/junk/callsite.log";
	private static final String LOG_LOCATION = "/mnt/logs/callsite.log";
	
	private static final Integer MAX_RESULTS = 50;

	
	private static final Comparator<CallsiteStatX> COMPARATOR = new CallsiteCountComparator();
//	private static final Comparator<CallsiteStat> COMPARATOR = new CallsiteDurationComparator();
	
	/**************** main ********************/
	
	public static void main(String... args){
		String report = new CallsiteAnalyzer(LOG_LOCATION, MAX_RESULTS).call();
		System.out.println(report);
	}
	
	
	/****************** fields *********************/

	private String logPath;
	private Integer maxResults;
	private Map<CallsiteStatKeyX,CallsiteStatX> aggregateStatByKey = new HashMap<>();
	
	
	/****************** construct ********************/
	
	public CallsiteAnalyzer(String logPath, Integer maxResults){
		this.logPath = logPath;
		this.maxResults = maxResults;
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
				if(ComparableTool.lt(record.getTimestamp(), firstDate)){ firstDate = record.getTimestamp(); }
				if(ComparableTool.gt(record.getTimestamp(), lastDate)){ lastDate = record.getTimestamp(); }
				CallsiteStatX stat = new CallsiteStatX(record.getCallsite(), record.getNodeName(), record
						.getDatarouterMethodName(), 1L, record.getDurationNs(), record.getNumItems());
				if(!aggregateStatByKey.containsKey(stat.getKey())){
					aggregateStatByKey.put(stat.getKey(), stat);
				}
				aggregateStatByKey.get(stat.getKey()).addMetrics(stat);
				if(numLines % 100000 == 0){
					logger.warn("scanned "+NumberFormatter.addCommas(numLines)+" in "+logPath);
				}
			}
		}
		
		//sort
		List<CallsiteStatX> stats = ListTool.createArrayList(aggregateStatByKey.values());
		Collections.sort(stats, Collections.reverseOrder(COMPARATOR));
		
		//build report
		StringBuilder sb = new StringBuilder();
		int numDaoCallsites = CallsiteStatX.countDaoCallsites(stats);
		long numSeconds = (lastDate.getTime() - firstDate.getTime()) / 1000;
		double callsPerSec = (double)numLines / (double)numSeconds;
		sb.append("          path: "+logPath+"\n");
		sb.append(" file size (B): "+NumberFormatter.addCommas(new File(logPath).length())+"\n");
		sb.append("         lines: "+NumberFormatter.addCommas(numLines)+"\n");
		sb.append("     callsites: "+NumberFormatter.addCommas(stats.size())+"\n");
		sb.append(" dao callsites: "+NumberFormatter.addCommas(numDaoCallsites)+"\n");
		sb.append("    first date: "+firstDate.toString()+"\n");
		sb.append("     last date: "+lastDate.toString()+"\n");
		sb.append("       seconds: "+NumberFormatter.addCommas(numSeconds)+"\n");
		sb.append("     calls/sec: "+NumberFormatter.format(callsPerSec, 2)+"\n");
		sb.append("\n");
		int rankWidth = 5;
		sb.append(StringTool.pad("rank", ' ', rankWidth) + CallsiteStatX.getReportHeader()+"\n");
		//print top N
		int row = 0;
		for(CallsiteStatX stat : stats){
			++row;
			if(row > maxResults){ break; }
			sb.append(StringTool.pad(row+"", ' ', rankWidth) + stat.getReportLine() + "\n");
		}
		return sb.toString();
	}
	
}
