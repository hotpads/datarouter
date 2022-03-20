/*
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

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.lang.LineOfCode;

@Singleton
public class CallsiteRecorder{
	private static final Logger logger = LoggerFactory.getLogger(CallsiteRecorder.class);

	public static void record(
			String nodeName,
			String datarouterMethodName,
			LineOfCode callsite,
			int numItems,
			long durationNs){
		if(!logger.isTraceEnabled()){
			return;
		}
		//currently rely on the logger timestamp, so pass null for timestamp
		var record = new CallsiteRecord(
				null,
				nodeName,
				datarouterMethodName,
				callsite.getPersistentString(),
				numItems,
				durationNs);
		logger.trace(record.getLogMessage());
	}

}
