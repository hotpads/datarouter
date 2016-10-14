package com.hotpads.trace.node;

import com.hotpads.datarouter.node.op.raw.write.StorageWriter;
import com.hotpads.trace.Trace;
import com.hotpads.trace.TraceSpan;
import com.hotpads.trace.TraceThread;
import com.hotpads.trace.key.TraceKey;
import com.hotpads.trace.key.TraceSpanKey;
import com.hotpads.trace.key.TraceThreadKey;

public interface TraceWriteNodes{

	StorageWriter<TraceKey,Trace> traceWriteQueue();
	StorageWriter<TraceThreadKey,TraceThread> traceThreadWriteQueue();
	StorageWriter<TraceSpanKey,TraceSpan> traceSpanWriteQueue();

}
