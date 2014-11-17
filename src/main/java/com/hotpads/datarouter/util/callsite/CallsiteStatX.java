package com.hotpads.datarouter.util.callsite;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.NumberFormatter;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.StringTool;


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
		return buildReportLine("count", "microSec", "avgMicroSec", "numItems", "type", "node", "method", "callsite");
	}
	
	private static final Set<String> HIDE_TIME_METHODS = SetTool.createHashSet("scanKeys", "scan");
	
	public String getReportLine(){
		String countString = NumberFormatter.addCommas(count);
		boolean hideDuration = HIDE_TIME_METHODS.contains(datarouterMethodName);
		String durationString = hideDuration ? "" : NumberFormatter.addCommas(getDurationUs());
		String avgCallUsString = hideDuration ? "" : NumberFormatter.addCommas(getDurationUs() / count);
		String numItemsString = NumberFormatter.addCommas(numItems);
		String type = isDaoCallsite() ? "dao" : "";
		return buildReportLine(countString, durationString, avgCallUsString, numItemsString, type, nodeName,
				datarouterMethodName, key.getCallsite());
	}
	
	private static String buildReportLine(String count, String durationUs, String avgCallUs, String numItems, String type, 
			String nodeName, String drMethod, String callsite){
		return StringTool.pad(count, ' ', 12)
				+ "  " + StringTool.pad(durationUs, ' ', 12)
				+ "  " + StringTool.pad(avgCallUs, ' ', 12)
				+ "  " + StringTool.pad(numItems, ' ', 12)
				+ "  " + StringTool.pad(type, ' ', 6)
//				+ "  " + StringTool.padEnd(nodeName, ' ', 60)
				+ "  " + StringTool.padEnd(drMethod, ' ', 20)
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
		String countString = NumberFormatter.addCommas(count);
		return StringTool.pad(countString, ' ', 8) + "   " + key.getCallsite();
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
		for(CallsiteStatX stat : IterableTool.nullSafe(stats)){
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