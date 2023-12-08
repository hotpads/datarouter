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
import java.util.regex.Pattern;

public class Traceparent{

	//TODO this pattern allows "b572a678d6c9194e", but then this fails: Long.parseLong("b572a678d6c9194e", 16)
	private static final Pattern TRACEPARENT_PATTERN = Pattern.compile(
			"^[0-9a-f]{2}-[0-9a-f]{32}-[0-9a-f]{16}-[0-9a-f]{2}$");
	private static final String TRACEPARENT_DELIMITER = "-";
	private static final Integer MIN_CHARS_TRACEPARENT = 55;
	private static final String CURRENT_VERSION = "00";

	public final String version = CURRENT_VERSION;
	public final String traceId;
	public final String parentId;
	private String traceFlags;

	/*-------- construct -------*/

	public Traceparent(String traceId, String parentId, String traceFlags){
		this.traceId = traceId;
		this.parentId = parentId;
		this.traceFlags = traceFlags;
	}

	public Traceparent(String traceId){
		this(traceId, TraceIdTool.newParentId());
	}

	public Traceparent(String traceId, String parentId){
		this(traceId, parentId, createDefaultTraceFlag());
	}

	public static Traceparent generateNew(long createdTimestampNs){
		return new Traceparent(
				TraceIdTool.newTraceId(createdTimestampNs),
				TraceIdTool.newParentId(),
				createDefaultTraceFlag());
	}

	public static Traceparent generateNewWithCurrentTimeInNs(){
		return new Traceparent(
				TraceIdTool.newTraceId(TraceTimeTool.epochNano()),
				TraceIdTool.newParentId(),
				createDefaultTraceFlag());
	}

	public Traceparent copyWithNewParentId(){
		return new Traceparent(traceId, TraceIdTool.newParentId(), traceFlags);
	}

	/*--------- methods --------*/

	public Instant getInstantTruncatedToMillis(){
		return TraceIdTool.toInstantTruncatedToMillis(traceId);
	}

	public static Optional<Traceparent> parseIfValid(String traceparentStr){
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
		try{// try/catch because invalid ids are passing the TRACEPARENT_PATTERN above
			return Optional.of(new Traceparent(tokens[1], tokens[2], tokens[3]));
		}catch(NumberFormatException e){
			return Optional.empty();
		}
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

	/*---------- Object --------*/

	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof Traceparent other)){
			return false;
		}
		return Objects.equals(version, other.version)
				&& Objects.equals(traceId, other.traceId)
				&& Objects.equals(parentId, other.parentId)
				&& Objects.equals(traceFlags, other.traceFlags);
	}

	@Override
	public int hashCode(){
		return Objects.hash(version, traceId, parentId, traceFlags);
	}

	@Override
	public String toString(){
		return toDelimitedString();
	}

	public String toDelimitedString(){
		return String.join(TRACEPARENT_DELIMITER, version, traceId, parentId, traceFlags);
	}

}
