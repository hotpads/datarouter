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
package io.datarouter.storage.profile.trace.node;

import io.datarouter.storage.node.entity.EntityNode;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.storage.node.op.raw.GroupQueueStorage;
import io.datarouter.storage.node.op.raw.write.StorageWriter;
import io.datarouter.storage.profile.trace.Trace;
import io.datarouter.storage.profile.trace.TraceEntity;
import io.datarouter.storage.profile.trace.TraceSpan;
import io.datarouter.storage.profile.trace.TraceThread;
import io.datarouter.storage.profile.trace.key.TraceEntityKey;
import io.datarouter.storage.profile.trace.key.TraceKey;
import io.datarouter.storage.profile.trace.key.TraceSpanKey;
import io.datarouter.storage.profile.trace.key.TraceThreadKey;
import io.datarouter.util.exception.NotImplementedException;

public interface TraceNodes extends TraceWriteNodes{

	EntityNode<TraceEntityKey,TraceEntity> entity();

	//write databeans to these; default to the persistent store
	@Override
	default StorageWriter<TraceKey,Trace> traceWriteQueue(){
		return trace();
	}

	@Override
	default StorageWriter<TraceThreadKey,TraceThread> traceThreadWriteQueue(){
		return traceThread();
	}

	@Override
	default StorageWriter<TraceSpanKey,TraceSpan> traceSpanWriteQueue(){
		return traceSpan();
	}


	//if writing to a queue, use these to read from the queue
	default GroupQueueStorage<TraceKey,Trace> traceReadQueue(){
		throw new NotImplementedException("no traceReadQueue configured");
	}

	default GroupQueueStorage<TraceThreadKey,TraceThread> traceThreadReadQueue(){
		throw new NotImplementedException("no traceThreadReadQueue configured");
	}

	default GroupQueueStorage<TraceSpanKey,TraceSpan> traceSpanReadQueue(){
		throw new NotImplementedException("no traceSpanReadQueue configured");
	}

	// persistent storage
	SortedMapStorage<TraceKey,Trace> trace();
	SortedMapStorage<TraceThreadKey,TraceThread> traceThread();
	SortedMapStorage<TraceSpanKey,TraceSpan> traceSpan();

}
