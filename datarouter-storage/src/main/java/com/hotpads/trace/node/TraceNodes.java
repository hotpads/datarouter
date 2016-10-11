package com.hotpads.trace.node;

import com.hotpads.datarouter.node.op.raw.GroupQueueStorage;
import com.hotpads.datarouter.node.op.raw.write.StorageWriter;
import com.hotpads.trace.Trace;
import com.hotpads.trace.TraceSpan;
import com.hotpads.trace.TraceThread;
import com.hotpads.trace.key.TraceKey;
import com.hotpads.trace.key.TraceSpanKey;
import com.hotpads.trace.key.TraceThreadKey;
import com.hotpads.util.core.exception.NotImplementedException;

public interface TraceNodes extends TraceWriteNodes{

	//write databeans to these; default to the persistent store
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


	//persistent storage
	TraceEntityNode traceEntity();

}
