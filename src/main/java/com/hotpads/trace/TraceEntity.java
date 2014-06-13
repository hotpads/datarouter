package com.hotpads.trace;

import java.util.NavigableSet;

import com.hotpads.datarouter.storage.Entity.BaseEntity;
import com.hotpads.trace.key.TraceEntityKey;

public class TraceEntity extends BaseEntity<TraceEntityKey>{

	public TraceEntity(TraceEntityKey key){
		super(key);
	}
	
	
	/********************* get databeans ************************/
	
	public NavigableSet<Trace> getTraces(){
		return getDatabeans(Trace.class);
	}
	
	public NavigableSet<TraceThread> getTraceThreads(){
		return getDatabeans(TraceThread.class);
	}
	
	public NavigableSet<TraceSpan> getTraceSpans(){
		return getDatabeans(TraceSpan.class);
	}

}
