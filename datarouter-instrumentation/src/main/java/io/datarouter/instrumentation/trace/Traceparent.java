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
	private static final int TRACE_ID_HEX_SIZE = 32;
	private static final int PARENT_ID_HEX_SIZE = 16;

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
		return new Traceparent(createNewTraceId(createdTimestamp), createNewParentId(),
				createNewTraceFlag());
	}

	public Traceparent updateParentId(){
		return new Traceparent(traceId, createNewParentId(), traceFlags);
	}

	/*
	 * TraceId is a 32 hex digit String. We convert the root request created unix time into lowercase base16
	 * and append it with a randomly generated long lowercase base16 representation.
	 * */
	private static String createNewTraceId(long createdTimestamp){
		return String.format("%016x", createdTimestamp) + String.format("%016x", new Random().nextLong());
	}

	/*
	 * ParentId is a 16 hex digit String. We use a randomly generated long and convert it into lowercase base16
	 * representation.
	 * */
	public static String createNewParentId(){
		return String.format("%016x", new Random().nextLong());
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
