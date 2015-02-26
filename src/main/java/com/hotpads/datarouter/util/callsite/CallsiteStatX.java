package com.hotpads.datarouter.util.callsite;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrNumberFormatter;
import com.hotpads.datarouter.util.core.DrSetTool;
import com.hotpads.datarouter.util.core.DrStringTool;


/************** CallsiteCount ********************/

//for some reason, the eclipse error highligher hates the name CallsiteStatKey, so add a random "X"
public class CallsiteStatX{
	
	private CallsiteStatKeyX key;
	private String nodeName;
	private String datarouterMethodName;
	private Long count;
	private Long durationNs;
	private Long numItems;
	

	public CallsiteStatX(String callsite, String nodeName, String datarouterMethodName, Long count, Long durationNs,
			Long numItems){
		this.key = new CallsiteStatKeyX(callsite);
		this.nodeName = nodeName;
		this.datarouterMethodName = datarouterMethodName;
		this.count = count;
		this.durationNs = durationNs;
		this.numItems = numItems;
	}
	
	
	/***************** methods ************************/
	
	public static String getReportHeader(){
		return buildReportLine("count", "microSec", "avgMicroSec", "numItems", "avgItems", "type", "node", "method",
				"callsite");
	}
	
	private static final Set<String> HIDE_TIME_METHODS = DrSetTool.createHashSet("scanKeys", "scan");
	
	public String getReportLine(){
		String countString = DrNumberFormatter.addCommas(count);
		boolean hideDuration = HIDE_TIME_METHODS.contains(datarouterMethodName);
		String durationString = hideDuration ? "" : DrNumberFormatter.addCommas(getDurationUs());
		String avgCallUsString = hideDuration ? "" : DrNumberFormatter.addCommas(getDurationUs() / count);
		String numItemsString = DrNumberFormatter.addCommas(numItems);
		String avgItemsString = DrNumberFormatter.addCommas(numItems / count);
		String type = isDaoCallsite() ? "dao" : "";
		return buildReportLine(countString, durationString, avgCallUsString, numItemsString, avgItemsString, type,
				nodeName, datarouterMethodName, key.getCallsite());
	}
	
	private static String buildReportLine(String count, String durationUs, String avgCallUs, String numItems,
			String avgItems, String type, String nodeName, String drMethod, String callsite){
		return DrStringTool.pad(count, ' ', 12)
				+ "  " + DrStringTool.pad(durationUs, ' ', 15)
				+ "  " + DrStringTool.pad(avgCallUs, ' ', 12)
				+ "  " + DrStringTool.pad(numItems, ' ', 12)
				+ "  " + DrStringTool.pad(avgItems, ' ', 9)
				+ "  " + DrStringTool.pad(type, ' ', 6)
//				+ "  " + StringTool.padEnd(nodeName, ' ', 60)
				+ "  " + DrStringTool.padEnd(drMethod, ' ', 20)
				+ "  " + callsite;
	}

	

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj){
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		CallsiteStatX other = (CallsiteStatX)obj;
		if(key == null){
			if(other.key != null) return false;
		}else if(!key.equals(other.key)) return false;
		return true;
	}
	
	@Override
	public String toString(){
		String countString = DrNumberFormatter.addCommas(count);
		return DrStringTool.pad(countString, ' ', 8) + "   " + key.getCallsite();
	}

	
	/**************** comparator ******************/

	public static class CallsiteCountComparator implements Comparator<CallsiteStatX>{
		@Override
		public int compare(CallsiteStatX a, CallsiteStatX b){
			return a.count.compareTo(b.count);
		}
	}
	
	public static class CallsiteDurationComparator implements Comparator<CallsiteStatX>{
		@Override
		public int compare(CallsiteStatX a, CallsiteStatX b){
			return a.durationNs.compareTo(b.durationNs);
		}
	}
	
	
	/***************** methods ****************************/
	
	public void addMetrics(CallsiteStatX other){
		this.count += other.count;
		this.durationNs += other.durationNs;
		this.numItems += other.numItems;
	}
	
	public Long getDurationUs(){
		return durationNs / 1000;
	}
	
	public boolean isDaoCallsite(){
		return key.getCallsite().toLowerCase().contains("dao");
	}
	
	/********************** static methods ****************/
	
	public static int countDaoCallsites(List<CallsiteStatX> stats){
		int numDaoCallsites = 0;
		for(CallsiteStatX stat : DrIterableTool.nullSafe(stats)){
			if(stat.isDaoCallsite()){
				++numDaoCallsites;
			}
		}
		return numDaoCallsites;
	}
	
	
	/******************* get/set ***************************/
	
	public Long getCount(){
		return count;
	}

	public CallsiteStatKeyX getKey(){
		return key;
	}

	public Long getDurationNs(){
		return durationNs;
	}
	
	public Long getNumItems(){
		return numItems;
	}


	
}