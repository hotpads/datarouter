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

public class TracerTool{

	public static String getTraceId(Tracer tracer){
		if(tracer == null){
			return null;
		}
		return tracer.getTraceId();
	}

	/*---------------------------- TraceThread ------------------------------*/

	public static Long getCurrentThreadId(Tracer tracer){
		if(tracer == null){
			return null;
		}
		return tracer.getCurrentThreadId();
	}

	public static void createAndStartThread(Tracer tracer, String name, long queueTimeMs){
		if(tracer == null){
			return;
		}
		tracer.createAndStartThread(name, queueTimeMs);
	}

	public static void createThread(Tracer tracer, String name, long queueTimeMs){
		if(tracer == null){
			return;
		}
		tracer.createThread(name, queueTimeMs);
	}

	public static void appendToThreadInfo(Tracer tracer, String text){
		if(tracer == null){
			return;
		}
		tracer.appendToThreadInfo(text);
	}

	public static void finishThread(Tracer tracer){
		if(tracer == null){
			return;
		}
		tracer.finishThread();
	}

	/*---------------------------- TraceSpan --------------------------------*/

	public static TraceSpanFinisher startSpan(Tracer tracer, String name){
		if(tracer == null){
			return new TraceSpanFinisher(tracer);
		}
		tracer.startSpan(name);
		return new TraceSpanFinisher(tracer);
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

}
