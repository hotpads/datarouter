package com.hotpads.trace;

import java.util.NavigableSet;

import com.hotpads.datarouter.storage.Entity.BaseEntity;
import com.hotpads.trace.key.TraceEntityKey;
import com.hotpads.trace.key.TraceKey;
import com.hotpads.trace.key.TraceSpanKey;
import com.hotpads.trace.key.TraceThreadKey;

public class TraceEntity extends BaseEntity<TraceEntityKey>{

	public TraceEntity(TraceEntityKey key){
		super(key);
	}
	
	@SuppressWarnings("unchecked") 
	public NavigableSet<Trace> getTraces(){
		String nodeName = 
		EntitySection<TraceEntityKey,TraceKey,Trace> section = (EntitySection<TraceEntityKey,TraceKey,Trace>)
				getDatabeansByNodeName().get(nodeName);
		return section==null ? null : section.getDatabeans();
	}
	
	@SuppressWarnings("unchecked") 
	public NavigableSet<TraceThread> getTraceThreads(){
		String nodeName = 
		EntitySection<TraceEntityKey,TraceThreadKey,TraceThread> section = (EntitySection<TraceEntityKey,TraceThreadKey,TraceThread>)
				getDatabeansByNodeName().get(nodeName);
		return section==null ? null : section.getDatabeans();
	}
	
	@SuppressWarnings("unchecked") 
	public NavigableSet<TraceSpan> getTraceSpans(){
		String nodeName = 
		EntitySection<TraceEntityKey,TraceSpanKey,TraceSpan> section = (EntitySection<TraceEntityKey,TraceSpanKey,TraceSpan>)
				getDatabeansByNodeName().get(nodeName);
		return section==null ? null : section.getDatabeans();
	}

}
