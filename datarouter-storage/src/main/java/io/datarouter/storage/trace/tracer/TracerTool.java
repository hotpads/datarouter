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
package io.datarouter.storage.trace.tracer;

public class TracerTool{

	public static Long getTraceId(Tracer tracer){
		if(tracer == null){
			return null;
		}
		return tracer.getTraceId();
	}

	/***************** Thread methods *******************/

	public static Long getCurrentThreadId(Tracer tracer){
		if(tracer == null){
			return null;
		}
		return tracer.getCurrentThreadId();
	}

	public static void createAndStartThread(Tracer tracer, String name){
		if(tracer == null){
			return;
		}
		tracer.createAndStartThread(name);
	}

	public static void createThread(Tracer tracer, String name){
		if(tracer == null){
			return;
		}
		tracer.createThread(name);
	}

	public static void appendToThreadName(Tracer tracer, String text){
		if(tracer == null){
			return;
		}
		tracer.appendToThreadName(text);
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

	/***************** Span methods ************************/

	public static void startSpan(Tracer tracer, String name){
		if(tracer == null){
			return;
		}
		tracer.startSpan(name);
	}

	public static void appendToSpanName(Tracer tracer, String text){
		if(tracer == null){
			return;
		}
		tracer.appendToSpanName(text);
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
