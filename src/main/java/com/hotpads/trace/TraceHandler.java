package com.hotpads.trace;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import javax.inject.Inject;

import com.hotpads.datarouter.storage.key.KeyTool;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.MessageMav;
import com.hotpads.handler.util.RequestTool;
import com.hotpads.trace.key.TraceEntityKey;
import com.hotpads.trace.key.TraceKey;
import com.hotpads.trace.key.TraceSpanKey;
import com.hotpads.trace.key.TraceThreadKey;
import com.hotpads.trace.node.TraceNodes;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.MapTool;

public class TraceHandler extends BaseHandler{
	
	private static final String
		JSP_viewTrace = "/jsp/admin/datarouter/trace/viewTrace.jsp";
	
	@Inject
	private TraceNodes traceNodes;
	
	@Override
	protected Mav handleDefault(){
		return new MessageMav("view a trace at /traces/viewTrace?id=123");
	}
	
	@Handler
	Mav viewTrace(){
		Mav mav = new Mav(JSP_viewTrace);
		Long id = RequestTool.getLong(request, "id");
		TraceEntityKey entityKey = new TraceEntityKey(id);
		
		/*
		 * testing entity nodes on real data.  unit tests in SortedNodeIntegrationTests
		 */
		Trace trace;
		List<TraceThread> threads;
		List<TraceSpan> spans;

		boolean useEntityMethod = RequestTool.getBoolean(request, "useEntityMethod", true);
		boolean useEntityNode = RequestTool.getBoolean(request, "useEntityNode", true);
		if(useEntityMethod){
			TraceEntity entity;
			if(useEntityNode){
				entity = traceNodes.traceEntity().getEntity(entityKey, null);
			}else{
				entity = traceNodes.traceCompound().getEntity(entityKey, null);
			}
			trace = entity.getTrace();
			threads = entity.getTraceThreads();
			spans = entity.getTraceSpans();
		}else{
			if(useEntityNode){
				trace = traceNodes.traceEntity().trace().get(new TraceKey(id), null);
				threads = traceNodes.traceEntity().thread().getWithPrefix(new TraceThreadKey(id, null), false, null);
				spans = traceNodes.traceEntity().span().getWithPrefix(new TraceSpanKey(id, null, null), false, null);
			}else{
				trace = traceNodes.traceCompound().trace().get(new TraceKey(id), null);
				threads = traceNodes.traceCompound().thread().getWithPrefix(new TraceThreadKey(id, null), false, null);
				spans = traceNodes.traceCompound().span().getWithPrefix(new TraceSpanKey(id, null, null), false, null);
			}
		}
		
		//get trace
		mav.put("trace", trace);
		
		//get threads
		if(CollectionTool.isEmpty(threads)){
			return new MessageMav("no threads found (yet)");
		}
		Map<TraceThreadKey,TraceThread> threadByKey = KeyTool.getByKeySorted(threads);
		TraceThreadGroup rootGroup = TraceThreadGroup.create(threads);
		mav.put("rootGroup", rootGroup);	
		mav.put("threads", rootGroup.getOrderedThreads());
		
		//spans
		rootGroup.setSpans(spans);
		SortedMap<TraceThreadKey,SortedSet<TraceSpan>> spansByThreadKey = TraceSpan.getByThreadKey(spans);
		SortedMap<TraceThreadKey,Long> missingTimeByThreadKey = new TreeMap<>();
		for(TraceThreadKey threadKey : MapTool.nullSafe(spansByThreadKey).keySet()){
			Long spansTime = TraceSpan.totalDurationOfNonChildren(spansByThreadKey.get(threadKey));
			TraceThread thread = threadByKey.get(threadKey);
			if(thread==null){ continue; }
			Long missingTime = thread.getRunningDuration() - spansTime;
			missingTimeByThreadKey.put(threadKey, missingTime);
		}
		mav.put("spansByThreadKey", spansByThreadKey);
		mav.put("missingTimeByThreadKey", missingTimeByThreadKey);
		
		mav.put("threadGroup", rootGroup);
		
		return mav;
	}
	
}
