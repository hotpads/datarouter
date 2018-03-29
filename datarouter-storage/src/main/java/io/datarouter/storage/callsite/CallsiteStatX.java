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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.datarouter.util.iterable.IterableTool;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.string.StringTool;


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
		String countString = NumberFormatter.addCommas(count);
		boolean hideDuration = HIDE_TIME_METHODS.contains(datarouterMethodName);
		String durationString = hideDuration ? "" : NumberFormatter.addCommas(getDurationUs());
		String avgCallUsString = hideDuration ? "" : NumberFormatter.addCommas(getDurationUs() / count);
		String numItemsString = NumberFormatter.addCommas(numItems);
		String avgItemsString = NumberFormatter.addCommas(numItems / count);
		String type = isDaoCallsite() ? DAO_CALLSITE_INDICATOR
				: StringTool.repeat(' ', DAO_CALLSITE_INDICATOR_LENGTH);
		return buildReportLine(metadata, countString, durationString, avgCallUsString, numItemsString, avgItemsString,
				type, key.getNodeName(), datarouterMethodName, key.getCallsite());
	}

	private static String buildReportLine(CallsiteStatReportMetadata reportMetadata, String count, String durationUs,
			String avgCallUs, String numItems, String avgItems, String type, String nodeName, String drMethod,
			String callsite){
		StringBuilder sb = new StringBuilder();
		sb.append(StringTool.pad(count, ' ', 2 + reportMetadata.getCountLength()));
		sb.append(StringTool.pad(durationUs, ' ', 2 + reportMetadata.getDurationUsLength()));
		sb.append(StringTool.pad(avgCallUs, ' ', 2 + reportMetadata.getAvgDurationUsLength()));
		sb.append(StringTool.pad(numItems, ' ', 2 + reportMetadata.getItemsLength()));
		sb.append(StringTool.pad(avgItems, ' ', 2 + reportMetadata.getAvgItemsLength()));
		sb.append(StringTool.pad(type, ' ', 2 + DAO_CALLSITE_INDICATOR_LENGTH));
		sb.append("  ");
		sb.append(StringTool.padEnd(nodeName, ' ', 2 + reportMetadata.getWidthNodeName()));
		sb.append(StringTool.padEnd(drMethod, ' ', 2 + reportMetadata.getWidthDatarouterMethod()));
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
		String countString = NumberFormatter.addCommas(count);
		return StringTool.pad(countString, ' ', 8) + "   " + key.getCallsite();
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
		for(CallsiteStatX stat : IterableTool.nullSafe(stats)){
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