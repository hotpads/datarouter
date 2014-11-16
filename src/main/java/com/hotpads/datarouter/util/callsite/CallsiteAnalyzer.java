package com.hotpads.datarouter.util.callsite;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.hotpads.datarouter.util.callsite.CallsiteStat.CallsiteCountComparator;
import com.hotpads.datarouter.util.callsite.CallsiteStat.CallsiteStatKey;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.io.ReaderTool;
import com.hotpads.util.core.iterable.scanner.Scanner;

public class CallsiteAnalyzer implements Callable<String>{
	
	// scp -i /hpdev/ec2-latest/.EC2_Keys/.ec2key root@webtest2:/mnt/logs/callsite.log .
	
//	private static final String LOG_LOCATION = "/mnt/hdd/junk/callsite.log";
	private static final String LOG_LOCATION = "/mnt/logs/callsite.log";

	
	private static final Comparator<CallsiteStat> COMPARATOR = new CallsiteCountComparator();
//	private static final Comparator<CallsiteStat> COMPARATOR = new CallsiteDurationComparator();
	
	/**************** main ********************/
	
	public static void main(String... args){
		String report = new CallsiteAnalyzer(LOG_LOCATION).call();
		System.out.println(report);
	}
	
	
	/****************** fields *********************/

	private String logPath;
	private Map<CallsiteStatKey,CallsiteStat> aggregateStatByKey = new HashMap<>();
	
	
	/****************** construct ********************/
	
	public CallsiteAnalyzer(String logPath){
		this.logPath = logPath;
	}
	
	@Override
	public String call(){
		//aggregate
		Scanner<List<String>> scanner = ReaderTool.scanFileLinesInBatches(logPath, 1000);
		while(scanner.advance()){
			List<String> batch = scanner.getCurrent();
			for(String line : batch){
				CallsiteRecord record = CallsiteRecord.fromLogLine(line);
				CallsiteStat stat = new CallsiteStat(record.getDatarouterMethodName(), record.getCallsite(), 1L, 
						record.getDurationNs());
				if(!aggregateStatByKey.containsKey(stat.getKey())){
					aggregateStatByKey.put(stat.getKey(), stat);
				}
				aggregateStatByKey.get(stat.getKey()).addMetrics(stat);
			}
		}
		
		//sort
		List<CallsiteStat> callsites = ListTool.createArrayList(aggregateStatByKey.values());
		Collections.sort(callsites, Collections.reverseOrder(COMPARATOR));
		
		
		//print top N
		StringBuilder sb = new StringBuilder();
		int row = 0;
		for(CallsiteStat stat : callsites){
			++row;
			if(row > 30){ return null; }
			sb.append(StringTool.pad(row+"", ' ', 3) + stat.getReportLine() + "\n");
		}
		return sb.toString();
	}
	
}
