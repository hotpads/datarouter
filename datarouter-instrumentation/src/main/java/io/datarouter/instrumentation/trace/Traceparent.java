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
package io.datarouter.instrumentation.trace;

import java.util.Random;

public class Traceparent{

	public static final String TRACEPARENT_DELIMITER = "-";
	public static final String CURRENT_VERSION = "00";

	public final String version = CURRENT_VERSION;
	public final String traceId;
	public final String parentId;
	public final String traceFlags;

	public Traceparent(String traceId, String parentId, String traceFlags){
		this.traceId = traceId;
		this.parentId = parentId;
		this.traceFlags = traceFlags;
	}

	public static Traceparent generateNew(long createdTimestamp){
		return new Traceparent(createNewRandomTraceId(), createParentIdByTimestamp(createdTimestamp),
				createNewTraceFlag());
	}

	public Traceparent updateParentId(long createdTimestamp){
		return new Traceparent(this.traceId, createParentIdByTimestamp(createdTimestamp), this.traceFlags);
	}

	/*
	 * TraceId is a 32 hex digit String. We convert a randomly generated integer to 8 digits hex (padded with 0s)
	 * and do the same for 4 times.
	 * */
	private static String createNewRandomTraceId(){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < 4; i++){
			sb.append(String.format("%08x", new Random().nextInt()));
		}
		return sb.toString();
	}

	/*
	 * ParentId is a 16 hex digit String. We convert the current unix timestamp to hex digits and padded with
	 * leading 0s.
	 * */
	public static String createParentIdByTimestamp(long currentTimestampInMillis){
		return String.format("%016x", currentTimestampInMillis);
	}

	// TODO: we need to update the logic to determine the traceflag
	private static String createNewTraceFlag(){
		return "01";
	}

	@Override
	public String toString(){
		return String.join(TRACEPARENT_DELIMITER, version, traceId, parentId, traceFlags);
	}

}
