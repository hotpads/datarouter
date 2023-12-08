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
package io.datarouter.instrumentation.trace;

import java.time.Instant;
import java.util.Random;

public class TraceIdTool{

	public static final int TRACE_ID_HEX_LENGTH = 32;
	public static final int PARENT_ID_HEX_LENGTH = 16;

	private static final int TRACE_ID_TIME_COMPONENT_HEX_LENGTH = 16; // 8 byte long as hex
	private static final String HEX_FORMAT = "%016x";

	/*
	 * traceId is a 32 char lowercase hex String.
	 * First 16 chars: epochNano long as lowercase hex.
	 * Last 16 chars: random long as lowercase hex.
	 */
	public static String newTraceId(long epochNano){
		return String.format(HEX_FORMAT, epochNano) + String.format(HEX_FORMAT, new Random().nextLong());
	}

	public static String newTraceId(Instant instant){
		return newTraceId(TraceTimeTool.epochNano(instant));
	}

	public static long traceIdToEpochNano(String traceId){
		String timeComponentString = traceId.substring(0, TRACE_ID_TIME_COMPONENT_HEX_LENGTH);
		return Long.parseLong(timeComponentString, TRACE_ID_TIME_COMPONENT_HEX_LENGTH);
	}

	public static Instant toInstantTruncatedToMillis(String traceId){
		long epochMilli = traceIdToEpochNano(traceId) / 1_000_000L;
		return Instant.ofEpochMilli(epochMilli);
	}

	/*
	 * parentId is a random long converted to 16 char lowercase hex String.
	 */
	public static String newParentId(){
		return String.format(HEX_FORMAT, new Random().nextLong());
	}

}
