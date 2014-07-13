package com.hotpads.trace;

import java.util.NavigableSet;

import com.hotpads.datarouter.storage.entity.BaseEntity;
import com.hotpads.trace.key.TraceEntityKey;

public class TraceEntity extends BaseEntity<TraceEntityKey>{
	
	//BaseEntity relies on these to store databeans, so they must be used by Trace Nodes to add databeans to the entity
	public static final String
		TABLE_Trace = "TestTrace",
		TABLE_TraceThread = "TestTraceThread",
		TABLE_TraceSpan = "TestTraceSpan";

	public TraceEntity(TraceEntityKey key){
		super(key);
	}
	
	
	/********************* get databeans ************************/
	
	public NavigableSet<Trace> getTraces(){
		return getDatabeansForTableName(Trace.class, TABLE_Trace);
	}
	
	public NavigableSet<TraceThread> getTraceThreads(){
		return getDatabeansForTableName(TraceThread.class, TABLE_TraceThread);
	}
	
	public NavigableSet<TraceSpan> getTraceSpans(){
		return getDatabeansForTableName(TraceSpan.class, TABLE_TraceSpan);
	}

}
