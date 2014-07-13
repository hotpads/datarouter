package com.hotpads.trace;

import java.util.NavigableSet;

import com.hotpads.datarouter.storage.entity.BaseEntity;
import com.hotpads.trace.key.TraceEntityKey;

public class TraceEntity extends BaseEntity<TraceEntityKey>{
	
	//BaseEntity relies on these to store databeans, so they must be used by Trace Nodes to add databeans to the entity
	public static final String
		QUALIFIER_PREFIX_Trace = "T",
		QUALIFIER_PREFIX_TraceThread = "TT",
		QUALIFIER_PREFIX_TraceSpan = "TS";
	
	private TraceEntity(){
		super(null);
	}

	public TraceEntity(TraceEntityKey key){
		super(key);
	}
	
	
	/********************* get databeans ************************/
	
	public NavigableSet<Trace> getTraces(){
		return getDatabeansForQualifierPrefix(Trace.class, QUALIFIER_PREFIX_Trace);
	}
	
	public NavigableSet<TraceThread> getTraceThreads(){
		return getDatabeansForQualifierPrefix(TraceThread.class, QUALIFIER_PREFIX_TraceThread);
	}
	
	public NavigableSet<TraceSpan> getTraceSpans(){
		return getDatabeansForQualifierPrefix(TraceSpan.class, QUALIFIER_PREFIX_TraceSpan);
	}

}
