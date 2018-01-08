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

import io.datarouter.storage.node.op.raw.write.StorageWriter;
import io.datarouter.storage.profile.trace.Trace;
import io.datarouter.storage.profile.trace.TraceSpan;
import io.datarouter.storage.profile.trace.TraceThread;
import io.datarouter.storage.profile.trace.key.TraceKey;
import io.datarouter.storage.profile.trace.key.TraceSpanKey;
import io.datarouter.storage.profile.trace.key.TraceThreadKey;

public interface TraceWriteNodes{

	StorageWriter<TraceKey,Trace> traceWriteQueue();
	StorageWriter<TraceThreadKey,TraceThread> traceThreadWriteQueue();
	StorageWriter<TraceSpanKey,TraceSpan> traceSpanWriteQueue();

}
