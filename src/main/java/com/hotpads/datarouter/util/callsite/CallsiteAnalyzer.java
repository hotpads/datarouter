package com.hotpads.datarouter.util.callsite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.NumberFormatter;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.io.ReaderTool;
import com.hotpads.util.core.iterable.scanner.Scanner;

public class CallsiteAnalyzer{
	
	private static final boolean COUNT_VS_MICROSECONDS = false;

	public static void main(String... args){
		//aggregate
		Map<String,Long> countByCallsite = new HashMap<>();
		Scanner<List<String>> scanner = ReaderTool.scanFileLinesInBatches("/mnt/logs/callsite.log", 1000);
		while(scanner.advance()){
			List<String> batch = scanner.getCurrent();
			for(String line : batch){
				CallsiteRecord record = CallsiteRecord.fromLong(line);
				long delta = COUNT_VS_MICROSECONDS ? 1 : record.getDurationUs();
				MapTool.increment(countByCallsite, record.getCallsite(), delta);
			}
		}
		
		//sort
		List<CallsiteCount> callsiteCounts = getSortedCallsiteCounts(countByCallsite);
		
		//print top N
		int row = 0;
		for(CallsiteCount callsiteCount : callsiteCounts){
			++row;
			if(row > 30){ return; }
			String callsite = callsiteCount.getCallsite();
			if(COUNT_VS_MICROSECONDS){
				String countString = NumberFormatter.addCommas(callsiteCount.getCount());
				System.out.println(StringTool.pad(row+"", ' ', 3) + " " + callsite + " " + countString + " calls");
			}else{
				String countString = NumberFormatter.addCommas(callsiteCount.getCount() / 1000);
				System.out.println(StringTool.pad(row+"", ' ', 3) + " " + callsite + " " + countString + " ms");
			}
		}
	}
	
	
	private static List<CallsiteCount> getSortedCallsiteCounts(Map<String,Long> countByCallsite){
		List<CallsiteCount> callsiteCounts = new ArrayList<>();
		for(Map.Entry<String,Long> entry : countByCallsite.entrySet()){
			callsiteCounts.add(new CallsiteCount(entry.getValue(), entry.getKey()));
		}
		Collections.sort(callsiteCounts);
		return callsiteCounts;
	}
	
}
