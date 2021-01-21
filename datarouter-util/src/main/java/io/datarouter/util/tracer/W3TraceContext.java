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
package io.datarouter.util.tracer;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.trace.TraceContext;
import io.datarouter.instrumentation.trace.Traceparent;
import io.datarouter.instrumentation.trace.Tracestate;

public class W3TraceContext implements TraceContext{
	private static final Logger logger = LoggerFactory.getLogger(W3TraceContext.class);

	public static final Pattern TRACEPARENT_PATTERN = Pattern.compile(
			"^[0-9a-f]{2}-[0-9a-f]{32}-[0-9a-f]{16}-[0-9a-f]{2}$");
	public static final Pattern TRACESTATE_PATTERN = Pattern.compile(
			"^([a-z0-9_\\-*/@]*=[a-zA-Z0-9]*)(,[a-z0-9_\\-*/@]*=[a-zA-Z0-9]*)*$");

	private static final Integer MIN_CHARS_TRACEPARENT = 55;

	private Traceparent traceparent;
	private Tracestate tracestate;
	private boolean hasValidTraceparent = false;

	/**
	 * @param traceparentStr the traceparent passed in the request header
	 * @param tracestateStr the tracestate passed in the request header
	 * @param createdTimestamp the timestamp used when existing traceparent is invalid or missing to create a parentId
	 */
	public W3TraceContext(String traceparentStr, String tracestateStr, long createdTimestamp){
		// If either trace-id, parent-id or trace-flags are invalid, we create a new traceparent and tracestate headers
		if(!validateAndSetTraceparent(traceparentStr)){
			traceparent = Traceparent.generateNew(createdTimestamp);
			tracestate = Tracestate.generateNew(traceparent.parentId);
		}
		parseOrCreateNewTracestate(tracestateStr);
	}

	public W3TraceContext(long createdTimestamp){
		traceparent = Traceparent.generateNew(createdTimestamp);
		tracestate = Tracestate.generateNew(traceparent.parentId);
	}

	@Override
	public Traceparent getTraceparent(){
		return traceparent;
	}

	@Override
	public Tracestate getTracestate(){
		return tracestate;
	}

	@Override
	public void updateParentIdAndAddTracestateMember(){
		traceparent = traceparent.updateParentId();
		tracestate.addDatarouterListMember(traceparent.parentId);
	}

	public Optional<Long> getTimestamp(){
		// only parentId created by datarouter can be translated into timestamp
		if(Tracestate.TRACESTATE_DR_KEY.equals(tracestate.getLastestTracestate().key)){
			return Optional.of(Long.parseLong(traceparent.traceId.substring(0, 16), 16));
		}
		return Optional.empty();
	}

	private boolean validateAndSetTraceparent(String traceparentStr){
		logger.debug("traceparent={} recieved.", traceparentStr);
		if(traceparentStr == null || traceparentStr.isEmpty()){
			return false;
		}else if(traceparentStr.length() < MIN_CHARS_TRACEPARENT){
			logger.warn("traceparent={} length is shorter than {}", traceparentStr, MIN_CHARS_TRACEPARENT);
			return false;
		}else if(!TRACEPARENT_PATTERN.matcher(traceparentStr).matches()){
			logger.warn("traceparent={} does not match w3 format", traceparentStr);
			return false;
		}
		String[] tokens = traceparentStr.split(Traceparent.TRACEPARENT_DELIMITER);
		if(!Traceparent.CURRENT_VERSION.equals(tokens[0])){
			logger.warn("traceparent version={} is not supported", tokens[0]);
			return false;
		}
		traceparent = new Traceparent(tokens[1], tokens[2], tokens[3]);
		hasValidTraceparent = true;
		return true;
	}

	private void parseOrCreateNewTracestate(String tracestateStr){
		logger.debug("tracestate={} recieved.", tracestateStr);
		if(!hasValidTraceparent){
			logger.debug("traceparent was not valid, generate a new tracestate");
			tracestate = Tracestate.generateNew(traceparent.parentId);
			return;
		}
		if(tracestateStr == null || tracestateStr.isEmpty()){
			logger.debug("traceparent is null or empty.");
			tracestate = Tracestate.generateNew(traceparent.parentId);
			return;
		}
		Matcher matcher = TRACESTATE_PATTERN.matcher(tracestateStr);
		if(!matcher.matches()){
			logger.warn("tracestate=\"{}\" does not match w3 format", tracestateStr);
			tracestate = Tracestate.generateNew(traceparent.parentId);
			return;
		}

		tracestate = new Tracestate();
		String[] members = tracestateStr.split(Tracestate.TRACESTATE_MEMBER_DELIMITER);
		for(String member : members){
			String[] tokens = member.split(Tracestate.TRACESTATE_KEYVALUE_DELIMITER);
			tracestate.addListMember(tokens[0], tokens[1]);
		}
		logger.debug("{} number of tracestate members found in {}.", members.length, tracestateStr);
	}

	@Override
	public String toString(){
		return "traceparent=\"" + traceparent + "\", tracestate=\"" + tracestate + "\"";
	}

}
