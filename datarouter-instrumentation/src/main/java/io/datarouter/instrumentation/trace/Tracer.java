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

import java.util.Optional;
import java.util.concurrent.BlockingQueue;

public interface Tracer{

	String getServerName();
	Optional<W3TraceContext> getTraceContext();
	BlockingQueue<TraceThreadDto> getThreadQueue();
	BlockingQueue<TraceSpanDto> getSpanQueue();

	Long getCurrentThreadId();
	Integer getDiscardedThreadCount();
	void incrementDiscardedThreadCount(int discardedThreadCount);

	default void createAndStartThread(String name, long queueTimeNs){
		createThread(name, queueTimeNs);
		startThread();
	}
	void setAlternativeStartTimeNs();
	Optional<Long> getAlternativeStartTimeNs();
	void createThread(String name, long queueTimeNs);
	void startThread();
	void addThread(TraceThreadDto thread);
	void appendToThreadInfo(String text);
	void finishThread();

	Integer getDiscardedSpanCount();
	void startSpan(String name, TraceSpanGroupType groupType, long createdTimeNs);
	void addSpan(TraceSpanDto span);
	void appendToSpanInfo(String text);
	void finishSpan(long endTimeNs);
	void incrementDiscardedSpanCount(int discardedSpanCount);
	TraceSpanDto getCurrentSpan();

	boolean shouldSample();
	void setForceSample();
	boolean shouldLog();
	void setForceLog();

	void setSaveThreadCpuTime(boolean saveThreadCpuTime);
	void setSaveThreadMemoryAllocated(boolean saveThreadMemoryAllocated);
	void setSaveSpanCpuTime(boolean saveSpanCpuTime);
	void setSaveSpanMemoryAllocated(boolean saveSpanMemoryAllocated);

	Tracer createChildTracer();

}
