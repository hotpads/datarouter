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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TracerTool{

	public static Optional<Traceparent> getTraceparent(Tracer tracer){
		if(tracer == null){
			return Optional.empty();
		}
		return tracer.getTraceContext()
				.map(W3TraceContext::getTraceparent);
	}

	public static Optional<Traceparent> getCurrentTraceparent(){
		return getTraceparent(TracerThreadLocal.get());
	}

	/*---------------------------- TraceThread ------------------------------*/

	public static void appendToThreadInfo(Tracer tracer, String text){
		if(tracer == null){
			return;
		}
		tracer.appendToThreadInfo(text);
	}

	/*---------------------------- TraceSpan --------------------------------*/

	public static TraceSpanFinisher startSpan(Tracer tracer, String name, TraceSpanGroupType groupType){
		if(tracer == null){
			return new TraceSpanFinisher(tracer);
		}
		tracer.startSpan(name, groupType);
		return new TraceSpanFinisher(tracer);
	}

	public static TraceSpanFinisher startSpan(String name, TraceSpanGroupType groupType){
		return startSpan(TracerThreadLocal.get(), name, groupType);
	}

	public static TraceSpanFinisher startSpanNoGroupType(String name){
		return startSpan(name, TraceSpanGroupType.NONE);
	}

	public static void appendToSpanInfo(String text){
		appendToSpanInfo(TracerThreadLocal.get(), text);
	}

	public static void appendToSpanInfo(String key, Object value){
		appendToSpanInfo(new TraceSpanInfoBuilder().add(key, value));
	}

	public static void appendToSpanInfo(TraceSpanInfoBuilder spanInfoBuilder){
		String text = spanInfoBuilder.joinEntries();
		if(text.isBlank()){
			return;
		}
		appendToSpanInfo(text);
	}

	public static void appendToSpanInfo(Tracer tracer, String text){
		if(tracer == null){
			return;
		}
		tracer.appendToSpanInfo(text);
	}

	public static void finishSpan(Tracer tracer){
		if(tracer == null){
			return;
		}
		tracer.finishSpan();
	}

	public static void finishSpan(){
		finishSpan(TracerThreadLocal.get());
	}

	public static void setForceLog(){
		Tracer tracer = TracerThreadLocal.get();
		if(tracer != null){
			tracer.setForceLog();
		}
	}

	public static void setForceSample(){
		Tracer tracer = TracerThreadLocal.get();
		if(tracer != null){
			tracer.setForceSample();
		}
	}

	public static boolean shouldSample(){
		Tracer tracer = TracerThreadLocal.get();
		if(tracer != null){
			return tracer.shouldSample();
		}
		return false;
	}

	public static boolean shouldLog(){
		Tracer tracer = TracerThreadLocal.get();
		if(tracer != null){
			return tracer.shouldLog();
		}
		return false;
	}

	public static class TraceSpanInfoBuilder{

		private final List<String> spanEntries = new ArrayList<>();

		public TraceSpanInfoBuilder add(String key, Object value){
			spanEntries.add(key + '=' + Objects.toString(value));
			return this;
		}

		public TraceSpanInfoBuilder databeans(Number count){
			return add("databeans", count);
		}

		public TraceSpanInfoBuilder keys(Number count){
			return add("keys", count);
		}

		public TraceSpanInfoBuilder ranges(Number count){
			return add("ranges", count);
		}

		public TraceSpanInfoBuilder bytes(Number count){
			return add("bytes", count);
		}

		public TraceSpanInfoBuilder rows(Number count){
			return add("rows", count);
		}

		public String joinEntries(){
			return String.join(", ", spanEntries);
		}

	}

}
