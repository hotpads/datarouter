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
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;

public class Traceparent{

	private static final Pattern TRACEPARENT_PATTERN = Pattern.compile(
			"^[0-9a-f]{2}-[0-9a-f]{32}-[0-9a-f]{16}-[0-9a-f]{2}$");
	private static final String TRACEPARENT_DELIMITER = "-";
	private static final Integer MIN_CHARS_TRACEPARENT = 55;
	private static final String CURRENT_VERSION = "00";
	public static final int TRACE_ID_HEX_SIZE = 32;
	public static final int PARENT_ID_HEX_SIZE = 16;

	public final String version = CURRENT_VERSION;
	public final String traceId;
	public final String parentId;
	private String traceFlags;

	public Traceparent(String traceId, String parentId, String traceFlags){
		this.traceId = traceId;
		this.parentId = parentId;
		this.traceFlags = traceFlags;
	}

	public Traceparent(String traceId){
		this(traceId, createNewParentId());
	}

	public Traceparent(String traceId, String parentId){
		this(traceId, parentId, createDefaultTraceFlag());
	}

	public static Traceparent generateNew(long createdTimestamp){
		return new Traceparent(createNewTraceId(createdTimestamp), createNewParentId(),
				createDefaultTraceFlag());
	}

	public static Traceparent generateNewWithCurrentTimeInNs(){
		return new Traceparent(createNewTraceId(Trace2Dto.getCurrentTimeInNs()), createNewParentId(),
				createDefaultTraceFlag());
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

	public long getTimestampInMs(){
		return Long.parseLong(traceId.substring(0, 16), 16);
	}

	public Instant getInstant(){
		return Instant.ofEpochMilli(getTimestampInMs());
	}

	/*----------- trace flags ------------*/

	private static String createDefaultTraceFlag(){
		return TraceContextFlagMask.DEFAULT.toHexCode();
	}

	public void enableSample(){
		this.traceFlags = TraceContextFlagMask.enableTrace(traceFlags);
	}

	public void enableLog(){
		this.traceFlags = TraceContextFlagMask.enableLog(traceFlags);
	}

	public boolean shouldSample(){
		return TraceContextFlagMask.isTraceEnabled(traceFlags);
	}

	public boolean shouldLog(){
		return TraceContextFlagMask.isLogEnabled(traceFlags);
	}

	@Override
	public String toString(){
		return String.join(TRACEPARENT_DELIMITER, version, traceId, parentId, traceFlags);
	}

	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof Traceparent)){
			return false;
		}
		Traceparent other = (Traceparent)obj;
		return Objects.equals(version, other.version)
				&& Objects.equals(traceId, other.traceId)
				&& Objects.equals(parentId, other.parentId)
				&& Objects.equals(traceFlags, other.traceFlags);
	}

	@Override
	public int hashCode(){
		return Objects.hash(version, traceId, parentId, traceFlags);
	}

	public static Optional<Traceparent> parse(String traceparentStr){
		if(traceparentStr == null || traceparentStr.isEmpty()){
			return Optional.empty();
		}else if(traceparentStr.length() < MIN_CHARS_TRACEPARENT){
			return Optional.empty();
		}else if(!TRACEPARENT_PATTERN.matcher(traceparentStr).matches()){
			return Optional.empty();
		}
		String[] tokens = traceparentStr.split(Traceparent.TRACEPARENT_DELIMITER);
		if(!Traceparent.CURRENT_VERSION.equals(tokens[0])){
			return Optional.empty();
		}
		return Optional.of(new Traceparent(tokens[1], tokens[2], tokens[3]));
	}

}
