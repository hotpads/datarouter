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

import java.util.Optional;
import java.util.concurrent.BlockingQueue;

public interface Tracer{

	String getServerName();
	String getTraceId();
	Optional<W3TraceContext> getTraceContext();
	BlockingQueue<TraceThreadDto> getThreadQueue();
	BlockingQueue<TraceSpanDto> getSpanQueue();

	Long getCurrentThreadId();
	Integer getDiscardedThreadCount();
	void incrementDiscardedThreadCount(int discardedThreadCount);

	default void createAndStartThread(String name, long queueTimeMs){
		createThread(name, queueTimeMs);
		startThread();
	}
	void createThread(String name, long queueTimeMs);
	void startThread();
	void addThread(TraceThreadDto thread);
	void appendToThreadInfo(String text);
	void finishThread();

	Integer getDiscardedSpanCount();
	void startSpan(String name);
	void addSpan(TraceSpanDto span);
	void appendToSpanInfo(String text);
	void finishSpan();
	void incrementDiscardedSpanCount(int discardedSpanCount);

	boolean getForceSave();
	void setForceSave();

	Tracer createChildTracer();

}
