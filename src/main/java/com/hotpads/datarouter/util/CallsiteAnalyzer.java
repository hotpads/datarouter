package com.hotpads.datarouter.util;

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
import com.ibm.icu.text.NumberFormat;

public class CallsiteAnalyzer{

	public static void main(String... args){
		//aggregate
		Map<String,Long> countByCallsite = new HashMap<>();
		Scanner<List<String>> scanner = ReaderTool.scanFileLinesInBatches("/mnt/logs/callsite.log", 1000);
		while(scanner.advance()){
			List<String> batch = scanner.getCurrent();
			for(String line : batch){
				String[] lineTokens = line.split(" ");
				String nodeName = lineTokens[6];
				String opName = lineTokens[7];
				String callsite = lineTokens[8];
				String numItems = lineTokens[9];
				MapTool.increment(countByCallsite, callsite);
			}
		}
		
		//sort
		List<CallsiteCount> callsiteCounts = getSortedCallsiteCounts(countByCallsite);
		
		//print top N
		int row = 0;
		for(CallsiteCount callsiteCount : callsiteCounts){
			++row;
			if(row > 30){ return; }
			System.out.println(StringTool.pad(row+"", ' ', 3) + " " + callsiteCount);
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
	
	
	
	
	/************** CallsiteCount ********************/
	
	public static class CallsiteCount implements Comparable<CallsiteCount>{
		Long count;
		String callsite;
		
		public CallsiteCount(Long count, String callsite){
			this.count = count;
			this.callsite = callsite;
		}

		@Override
		public int hashCode(){
			final int prime = 31;
			int result = 1;
			result = prime * result + ((callsite == null) ? 0 : callsite.hashCode());
			result = prime * result + ((count == null) ? 0 : count.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj){
			if(this == obj) return true;
			if(obj == null) return false;
			if(getClass() != obj.getClass()) return false;
			CallsiteCount other = (CallsiteCount)obj;
			if(callsite == null){
				if(other.callsite != null) return false;
			}else if(!callsite.equals(other.callsite)) return false;
			if(count == null){
				if(other.count != null) return false;
			}else if(!count.equals(other.count)) return false;
			return true;
		}
		
		@Override
		public int compareTo(CallsiteCount that){
			return that.count.compareTo(this.count);
		}
		
		@Override
		public String toString(){
			String countString = NumberFormatter.addCommas(count);
			return StringTool.pad(countString, ' ', 8) + "   " + callsite;
		}
	}
	
}
