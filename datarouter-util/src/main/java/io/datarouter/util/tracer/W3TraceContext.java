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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.tuple.Pair;

public class W3TraceContext{
	private static final Logger logger = LoggerFactory.getLogger(W3TraceContext.class);

	public static final Pattern TRACEPARENT_PATTERN = Pattern.compile(
			"^[0-9a-f]{2}-[0-9a-f]{32}-[0-9a-f]{16}-[0-9a-f]{2}$");
	public static final Pattern TRACESTATE_PATTERN = Pattern.compile(
			"^([a-z0-9_\\-*/@]*=[a-zA-Z0-9]*)(,[a-z0-9_\\-*/@]*=[a-zA-Z0-9]*)*$");
	public static final String TRACESTATE_DR_KEY = "datarouter";
	private static final Integer MIN_CHARS_TRACEPARENT = 55;
	private static final String CURRENT_VERSION = "00";
	private static final String TRACEPARENT_DELIMITER = "-";
	private static final String TRACESTATE_KEYVALUE_DELIMITER = "=";
	private static final String TRACESTATE_MEMBER_DELIMITER = ",";

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

	public Traceparent getTraceparent(){
		return traceparent;
	}

	public Tracestate getTracestate(){
		return tracestate;
	}

	public Optional<Long> getTimestamp(){
		// only parentId created by datarouter can be translated into timestamp
		if(TRACESTATE_DR_KEY.equals(tracestate.getLastestTracestate().getLeft())){
			return Optional.of(Long.parseLong(traceparent.parentId, 16));
		}
		return Optional.empty();
	}

	private boolean validateAndSetTraceparent(String traceparentStr){
		logger.debug("traceparent={} recieved.", traceparentStr);
		if(traceparentStr == null || traceparentStr.isEmpty()){
			logger.warn("traceparent is null or empty");
			return false;
		}else if(traceparentStr.length() < MIN_CHARS_TRACEPARENT){
			logger.warn("traceparent={} length is shorter than {}", traceparentStr, MIN_CHARS_TRACEPARENT);
			return false;
		}else if(!TRACEPARENT_PATTERN.matcher(traceparentStr).matches()){
			logger.warn("traceparent={} does not match w3 format", traceparentStr);
			return false;
		}
		String[] tokens = traceparentStr.split(TRACEPARENT_DELIMITER);
		if(!CURRENT_VERSION.equals(tokens[0])){
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
			logger.warn("traceparent was not valid, generate a new tracestate");
			tracestate = Tracestate.generateNew(traceparent.parentId);
			return;
		}
		if(tracestateStr == null || tracestateStr.isEmpty()){
			logger.warn("traceparent is null or empty.");
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
		String[] members = tracestateStr.split(TRACESTATE_MEMBER_DELIMITER);
		for(String member : members){
			String[] tokens = member.split(TRACESTATE_KEYVALUE_DELIMITER);
			tracestate.addListMember(tokens[0], tokens[1]);
		}
		logger.info("{} number of tracestate members found in {}.", members.length, tracestateStr);
	}

	public static class Traceparent{

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

	public static class Tracestate{

		private Map<String,String> tracestateMap = new LinkedHashMap<>();

		public void addListMember(String key, String value){
			tracestateMap.put(key, value);
		}

		public Pair<String,String> getLastestTracestate(){
			return tracestateMap.entrySet().stream()
					.map(entry -> new Pair<>(entry.getKey(), entry.getValue()))
					.findFirst()
					.get();
		}

		/**
		 * @param parentId parentId in traceparent
		 */
		private static Tracestate generateNew(String parentId){
			Tracestate tracestate = new Tracestate();
			tracestate.addListMember(TRACESTATE_DR_KEY, parentId);
			return tracestate;
		}

		@Override
		public String toString(){
			return tracestateMap.entrySet().stream()
					.map(entry -> entry.getKey() + TRACESTATE_KEYVALUE_DELIMITER + entry.getValue())
					.collect(Collectors.joining(TRACESTATE_MEMBER_DELIMITER));
		}

	}

}
