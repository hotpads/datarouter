package com.hotpads.datarouter.util.callsite;

import com.hotpads.datarouter.util.core.DrNumberFormatter;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.stream.StreamTool;

public class CallsiteStatReportMetadata{

	private long maxCount = 0;
	private long maxDurationUs = 0;
	private long maxAvgDurationUs = 0;
	private long maxItems = 0;
	private long maxAvgItems = 0;
	private int widthNodeName = 0;
	private int widthDatarouterMethod = 0;

	public void inspect(CallsiteStatX stat){
		maxCount = Math.max(maxCount, stat.getCount());
		maxDurationUs = Math.max(maxDurationUs, stat.getDurationNs());
		maxAvgDurationUs = Math.max(maxAvgDurationUs, stat.getAvgDurationUs());
		maxItems = Math.max(maxItems, stat.getNumItems());
		maxAvgItems = Math.max(maxAvgItems, stat.getAvgItems());
		widthNodeName = Math.max(widthNodeName, DrStringTool.length(stat.getKey().getNodeName()));
		widthDatarouterMethod = Math.max(widthDatarouterMethod, DrStringTool.length(stat.getDatarouterMethodName()));
	}


	/********************* static ******************************/

	public static CallsiteStatReportMetadata inspect(Iterable<CallsiteStatX> stats){
		CallsiteStatReportMetadata reportMetadata = new CallsiteStatReportMetadata();
		StreamTool.forEach(stats, stat -> reportMetadata.inspect(stat));
		return reportMetadata;
	}


	/******************* methods ********************************/

	public int getCountLength(){
		return Math.max(CallsiteReportHeader.count.length(), DrNumberFormatter.addCommas(maxCount).length());
	}

	public int getDurationUsLength(){
		return Math.max(CallsiteReportHeader.microSec.length(), DrNumberFormatter.addCommas(maxDurationUs).length());
	}

	public int getAvgDurationUsLength(){
		return Math.max(CallsiteReportHeader.avgMicroSec.length(), DrNumberFormatter.addCommas(maxAvgDurationUs)
				.length());
	}

	public int getItemsLength(){
		return Math.max(CallsiteReportHeader.numItems.length(), DrNumberFormatter.addCommas(maxItems).length());
	}

	public int getAvgItemsLength(){
		return Math.max(CallsiteReportHeader.avgItems.length(), DrNumberFormatter.addCommas(maxAvgItems).length());
	}

	public int getWidthNodeName(){
		return Math.max(CallsiteReportHeader.node.length(), widthNodeName);
	}

	public int getWidthDatarouterMethod(){
		return Math.max(CallsiteReportHeader.method.length(), widthDatarouterMethod);
	}

}
