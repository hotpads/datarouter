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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Tracestate{

	public static final String TRACESTATE_DR_KEY = "datarouter";
	public static final String TRACESTATE_KEYVALUE_DELIMITER = "=";
	public static final String TRACESTATE_MEMBER_DELIMITER = ",";

	private Map<String,String> tracestateMap = new LinkedHashMap<>();

	public void addDatarouterListMember(String value){
		tracestateMap.put(TRACESTATE_DR_KEY, value);
	}

	public void addListMember(String key, String value){
		tracestateMap.put(key, value);
	}

	public TracestateMemeber getLastestTracestate(){
		return tracestateMap.entrySet().stream()
				.map(entry -> new TracestateMemeber(entry.getKey(), entry.getValue()))
				.findFirst()
				.get();
	}

	/**
	 * @param parentId parentId in traceparent
	 */
	public static Tracestate generateNew(String parentId){
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

	public static class TracestateMemeber{

		public final String key;
		public final String value;

		public TracestateMemeber(String key, String value){
			this.key = key;
			this.value = value;
		}

	}

}
