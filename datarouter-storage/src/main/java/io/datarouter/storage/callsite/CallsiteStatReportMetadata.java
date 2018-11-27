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

import io.datarouter.util.iterable.IterableTool;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.string.StringTool;

public class CallsiteStatReportMetadata{

	private long maxCount = 0;
	private long maxDurationUs = 0;
	private long maxAvgDurationUs = 0;
	private long maxItems = 0;
	private long maxAvgItems = 0;
	private int widthNodeName = 0;
	private int widthDatarouterMethod = 0;

	public void inspect(CallsiteStat stat){
		maxCount = Math.max(maxCount, stat.getCount());
		maxDurationUs = Math.max(maxDurationUs, stat.getDurationNs());
		maxAvgDurationUs = Math.max(maxAvgDurationUs, stat.getAvgDurationUs());
		maxItems = Math.max(maxItems, stat.getNumItems());
		maxAvgItems = Math.max(maxAvgItems, stat.getAvgItems());
		widthNodeName = Math.max(widthNodeName, StringTool.length(stat.getKey().getNodeName()));
		widthDatarouterMethod = Math.max(widthDatarouterMethod, StringTool.length(stat.getDatarouterMethodName()));
	}

	public static CallsiteStatReportMetadata inspect(Iterable<CallsiteStat> stats){
		CallsiteStatReportMetadata reportMetadata = new CallsiteStatReportMetadata();
		IterableTool.forEach(stats, stat -> reportMetadata.inspect(stat));
		return reportMetadata;
	}

	public int getCountLength(){
		return Math.max(CallsiteReportHeader.count.length(), NumberFormatter.addCommas(maxCount).length());
	}

	public int getDurationUsLength(){
		return Math.max(CallsiteReportHeader.microSec.length(), NumberFormatter.addCommas(maxDurationUs).length());
	}

	public int getAvgDurationUsLength(){
		return Math.max(CallsiteReportHeader.avgMicroSec.length(), NumberFormatter.addCommas(maxAvgDurationUs)
				.length());
	}

	public int getItemsLength(){
		return Math.max(CallsiteReportHeader.numItems.length(), NumberFormatter.addCommas(maxItems).length());
	}

	public int getAvgItemsLength(){
		return Math.max(CallsiteReportHeader.avgItems.length(), NumberFormatter.addCommas(maxAvgItems).length());
	}

	public int getWidthNodeName(){
		return Math.max(CallsiteReportHeader.node.length(), widthNodeName);
	}

	public int getWidthDatarouterMethod(){
		return Math.max(CallsiteReportHeader.method.length(), widthDatarouterMethod);
	}

}
