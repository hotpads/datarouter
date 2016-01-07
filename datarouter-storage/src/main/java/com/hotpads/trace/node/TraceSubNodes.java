package com.hotpads.trace.node;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.trace.Trace;
import com.hotpads.trace.TraceSpan;
import com.hotpads.trace.TraceThread;
import com.hotpads.trace.key.TraceKey;
import com.hotpads.trace.key.TraceSpanKey;
import com.hotpads.trace.key.TraceThreadKey;

public interface TraceSubNodes{

	SortedMapStorageNode<TraceKey,Trace> trace();
	SortedMapStorageNode<TraceThreadKey,TraceThread> thread();
	SortedMapStorageNode<TraceSpanKey,TraceSpan> span();
	
}
