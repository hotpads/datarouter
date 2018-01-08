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
package io.datarouter.storage.profile.trace;

import java.util.List;

//naming of this class and its method is temporary.  will be migrated to better names
public interface Tracer{

	String getServerName();
	Long getTraceId();
	List<TraceThread> getThreads();
	List<TraceSpan> getSpans();

	Long getCurrentThreadId();

	void createAndStartThread(String name);
	void createThread(String name);
	void startThread();
	void appendToThreadName(String text);
	void appendToThreadInfo(String text);
	void finishThread();

	void startSpan(String name);
	void appendToSpanName(String text);
	void appendToSpanInfo(String text);
	void finishSpan();

}
