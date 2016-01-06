package com.hotpads.datarouter.util.callsite;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrNumberFormatter;
import com.hotpads.datarouter.util.core.DrStringTool;


/************** CallsiteCount ********************/

//for some reason, the eclipse error highlighter hates the name CallsiteStatKey, so add a random "X"
public class CallsiteStatX{

	private static final String DAO_CALLSITE_INDICATOR = "dao";
	private static final int DAO_CALLSITE_INDICATOR_LENGTH = Math.max(CallsiteReportHeader.type.length(),
			DAO_CALLSITE_INDICATOR.length());

	private CallsiteStatKeyX key;
	private String datarouterMethodName;
	private Long count;
	private Long durationNs;
	private Long numItems;


	public CallsiteStatX(String callsite, String nodeName, String datarouterMethodName, Long count, Long durationNs,
			Long numItems){
		this.key = new CallsiteStatKeyX(callsite, nodeName);
		this.datarouterMethodName = datarouterMethodName;
		this.count = count;
		this.durationNs = durationNs;
		this.numItems = numItems;
	}


	/***************** methods ************************/

	public static String getReportHeader(CallsiteStatReportMetadata metadata){
		return buildReportLine(metadata,
				CallsiteReportHeader.count.toString(),
				CallsiteReportHeader.microSec.toString(),
				CallsiteReportHeader.avgMicroSec.toString(),
				CallsiteReportHeader.numItems.toString(),
				CallsiteReportHeader.avgItems.toString(),
				CallsiteReportHeader.type.toString(),
				CallsiteReportHeader.node.toString(),
				CallsiteReportHeader.method.toString(),
				CallsiteReportHeader.callsite.toString());
	}

	private static final Set<String> HIDE_TIME_METHODS = new HashSet<>(Arrays.asList("scanKeys", "scan"));

	public String getReportLine(CallsiteStatReportMetadata metadata){
		String countString = DrNumberFormatter.addCommas(count);
		boolean hideDuration = HIDE_TIME_METHODS.contains(datarouterMethodName);
		String durationString = hideDuration ? "" : DrNumberFormatter.addCommas(getDurationUs());
		String avgCallUsString = hideDuration ? "" : DrNumberFormatter.addCommas(getDurationUs() / count);
		String numItemsString = DrNumberFormatter.addCommas(numItems);
		String avgItemsString = DrNumberFormatter.addCommas(numItems / count);
		String type = isDaoCallsite() ? DAO_CALLSITE_INDICATOR : DrStringTool.repeat(' ', DAO_CALLSITE_INDICATOR_LENGTH
				);
		return buildReportLine(metadata, countString, durationString, avgCallUsString, numItemsString, avgItemsString,
				type, key.getNodeName(), datarouterMethodName, key.getCallsite());
	}

	private static String buildReportLine(CallsiteStatReportMetadata reportMetadata, String count, String durationUs,
			String avgCallUs, String numItems, String avgItems, String type, String nodeName, String drMethod,
			String callsite){
		StringBuilder sb = new StringBuilder();
		sb.append(DrStringTool.pad(count, ' ', 2 + reportMetadata.getCountLength()));
		sb.append(DrStringTool.pad(durationUs, ' ', 2 + reportMetadata.getDurationUsLength()));
		sb.append(DrStringTool.pad(avgCallUs, ' ', 2 + reportMetadata.getAvgDurationUsLength()));
		sb.append(DrStringTool.pad(numItems, ' ', 2 + reportMetadata.getItemsLength()));
		sb.append(DrStringTool.pad(avgItems, ' ', 2 + reportMetadata.getAvgItemsLength()));
		sb.append(DrStringTool.pad(type, ' ', 2 + DAO_CALLSITE_INDICATOR_LENGTH));
		sb.append("  ");
		sb.append(DrStringTool.padEnd(nodeName, ' ', 2 + reportMetadata.getWidthNodeName()));
		sb.append(DrStringTool.padEnd(drMethod, ' ', 2 + reportMetadata.getWidthDatarouterMethod()));
		sb.append(callsite);
		return sb.toString();
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + (key == null ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if(getClass() != obj.getClass()){
			return false;
		}
		CallsiteStatX other = (CallsiteStatX)obj;
		if(key == null){
			if(other.key != null){
				return false;
			}
		}else if(!key.equals(other.key)){
			return false;
		}
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
		public int compare(CallsiteStatX callsiteA, CallsiteStatX callsiteB){
			return callsiteA.count.compareTo(callsiteB.count);
		}
	}

	public static class CallsiteDurationComparator implements Comparator<CallsiteStatX>{
		@Override
		public int compare(CallsiteStatX callsiteA, CallsiteStatX callsiteB){
			return callsiteA.durationNs.compareTo(callsiteB.durationNs);
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


	/****************** methods **********************************/

	public Long getAvgDurationUs(){
		return durationNs / count;
	}

	public Long getAvgItems(){
		return numItems / count;
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

	public String getDatarouterMethodName(){
		return datarouterMethodName;
	}

}