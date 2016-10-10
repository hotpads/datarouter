package com.hotpads.trace.node;

import com.hotpads.datarouter.node.op.raw.write.StorageWriter;
import com.hotpads.trace.Trace;
import com.hotpads.trace.TraceSpan;
import com.hotpads.trace.TraceThread;
import com.hotpads.trace.key.TraceKey;
import com.hotpads.trace.key.TraceSpanKey;
import com.hotpads.trace.key.TraceThreadKey;

public interface TraceNodes extends TraceWriteNodes{

	TraceEntityNode traceEntity();

	@Override
	default StorageWriter<TraceKey,Trace> traceWriteQueue(){
		return traceEntity().trace();
	}

	@Override
	default StorageWriter<TraceThreadKey,TraceThread> traceThreadWriteQueue(){
		return traceEntity().thread();
	}

	@Override
	default StorageWriter<TraceSpanKey,TraceSpan> traceSpanWriteQueue(){
		return traceEntity().span();
	}

}
