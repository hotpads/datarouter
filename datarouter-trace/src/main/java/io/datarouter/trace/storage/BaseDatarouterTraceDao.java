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
package io.datarouter.trace.storage;

import java.util.Collection;

import io.datarouter.trace.storage.entity.TraceEntity;
import io.datarouter.trace.storage.entity.TraceEntityKey;
import io.datarouter.trace.storage.span.TraceSpan;
import io.datarouter.trace.storage.thread.TraceThread;
import io.datarouter.trace.storage.trace.Trace;

public interface BaseDatarouterTraceDao{

	void putMulti(Collection<TraceThread> threads, Collection<TraceSpan> spans, Trace trace);
	TraceEntity getEntity(TraceEntityKey key);

	static class NoOpDatarouterTraceDao implements BaseDatarouterTraceDao{

		@Override
		public void putMulti(Collection<TraceThread> threads, Collection<TraceSpan> spans, Trace trace){
		}

		@Override
		public TraceEntity getEntity(TraceEntityKey key){
			return null;
		}

	}

}
