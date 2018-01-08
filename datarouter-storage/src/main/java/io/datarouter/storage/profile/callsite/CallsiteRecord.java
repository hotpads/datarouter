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
package io.datarouter.storage.profile.callsite;

import java.util.Date;

import io.datarouter.util.DateTool;


public class CallsiteRecord{

	private Date timestamp;
	private String nodeName;
	private String datarouterMethodName;
	private String callsite;
	private long numItems;
	private long durationNs;


	/************** construct ****************/

	public CallsiteRecord(Date timestamp, String nodeName, String datarouterMethodName, String callsite, long numItems,
			long durationNs){
		this.timestamp = timestamp;
		this.nodeName = nodeName;
		this.datarouterMethodName = datarouterMethodName;
		this.callsite = callsite;
		this.numItems = numItems;
		this.durationNs = durationNs;
	}


	/****************** serialize *******************/

	public String getLogMessage(){
		long durationUs = durationNs / 1000;
		String message = nodeName
				+ " " + datarouterMethodName
				+ " " + callsite
				+ " " + numItems
				+ " " + durationUs;
		return message;
	}

	public static CallsiteRecord fromLogLine(String line){
		String[] allTokens = line.split(" ");
		String dateTime = allTokens[0] + " " + allTokens[1];
		Date timestamp = DateTool.parseUserInputDate(dateTime, 2014);

		String afterThreadName = line.substring(line.indexOf("]") + 1);
		String[] lineTokens = afterThreadName.split(" ");
		int index = 3;
		String nodeName = lineTokens[index++];
		String datarouterMethodName = lineTokens[index++];
		String callsite = lineTokens[index++];
		Integer numItems = Integer.valueOf(lineTokens[index++]);
		Long microseconds = Long.valueOf(lineTokens[index++]);
		Long nanoseconds = 1000 * microseconds;
		return new CallsiteRecord(timestamp, nodeName, datarouterMethodName, callsite, numItems, nanoseconds);
	}


	/**************** methods *************************/

	public long getDurationUs(){
		return durationNs / 1000;
	}


	/***************** get/set ***********************/

	public String getNodeName(){
		return nodeName;
	}

	public String getDatarouterMethodName(){
		return datarouterMethodName;
	}

	public String getCallsite(){
		return callsite;
	}

	public long getNumItems(){
		return numItems;
	}

	public long getDurationNs(){
		return durationNs;
	}

	public Date getTimestamp(){
		return timestamp;
	}

}
