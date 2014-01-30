package com.hotpads.trace;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hotpads.profile.count.collection.Counters;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.StringTool;


public class TraceContext {
	protected static Logger logger = Logger.getLogger(TraceContext.class);
	
	protected String serverName;
	protected Long traceId;
	protected Long traceThreadParentId;
	protected Integer nextSpanSequence = 0;
	
	protected TraceThread currentThread;//should we be holding a map of current threads?  not sure yet
	protected List<TraceThread> threads = ListTool.createLinkedList();
	
	protected ArrayList<TraceSpan> spanStack = new ArrayList<TraceSpan>();
	protected List<TraceSpan> spans = ListTool.createLinkedList();
	
	
	/***************** constructors **********************************/
	
	public TraceContext(String serverName, Long traceId, Long traceThreadParentId){
		this.serverName = serverName;
		this.traceId = traceId;
		this.traceThreadParentId = traceThreadParentId;
	}

	
	/***************** static Trace methods ********************************************/
	
	
	
	/*************** static TraceThread methods *************************************/
	
	public Long getCurrentThreadId(){
//		TraceContext ctx = get();
//		if(ctx==null){ return null; }
		if(getCurrentThread()==null){ return null; }
		return getCurrentThread().getId();
	}
	
	public static void createAndStartThread(String name){
		TraceContext ctx = get();
		if(ctx==null){ return; }
		createThread(name);
		startThread();
	}
	
	public static void createThread(String name){
		TraceContext ctx = get();
		if(ctx==null){ return; }
		Long traceId = ctx.getTraceId();
		if(traceId==null){ return; }
		boolean hasParent = ctx.getTraceThreadParentId()!=null;
		TraceThread thread = new TraceThread(traceId, hasParent);
		thread.setParentId(ctx.getTraceThreadParentId());
		thread.setServerId(ctx.getServerName());
		thread.setName(name);
		ctx.setCurrentThread(thread);
	}
	
	public static void startThread(){
		TraceContext ctx = get();
		if(ctx==null){ return; }
		if(ctx.getCurrentThread()==null){ return; }
		ctx.getCurrentThread().markStart();
	}
	
	public static void appendToThreadName(String text){
		TraceContext ctx = get();
		if(ctx==null || ctx.getCurrentThread()==null){ return; }
		TraceThread thread = ctx.getCurrentThread();
		boolean addSpace = StringTool.notEmpty(thread.getName());
		thread.setName(StringTool.nullSafe(thread.getName()) + (addSpace ? " " : "") + text);
	}
	
	public static void appendToThreadInfo(String text){
		TraceContext ctx = get();
		if(ctx==null || ctx.getCurrentThread()==null){ return; }
		TraceThread thread = ctx.getCurrentThread();
		boolean addSpace = StringTool.notEmpty(thread.getInfo());
		thread.setInfo(StringTool.nullSafe(thread.getInfo()) + (addSpace ? " " : "") + text);
	}
	
	public static void finishThread(){
		TraceContext ctx = get();
		if(ctx==null){ return; }
		if(ctx.getCurrentThread()==null){ return; }
		TraceThread thread = ctx.getCurrentThread();
		thread.markFinish();
		ctx.getThreads().add(thread);
		ctx.setCurrentThread(null);
	}
	
	/*************** static TraceSpan methods *************************************/
		
	public static void startSpan(String name){
		Counters.inc(name);
		TraceContext ctx = get();
		if(ctx==null || ctx.currentThread==null){ return; }
		Integer parentSequence = null;
		if(CollectionTool.notEmpty(ctx.getSpanStack())){
			TraceSpan parent = ctx.getSpanStack().get(ctx.getSpanStack().size()-1);
			parentSequence = parent.getSequence();
		}
		TraceSpan span = new TraceSpan(
				ctx.currentThread.getTraceId(), 
				ctx.currentThread.getId(), 
				ctx.nextSpanSequence,
				parentSequence);
		span.setName(name);
		ctx.getSpanStack().add(span);
		++ctx.nextSpanSequence;
	}
	
	/*
	 * Use this method carefully, because span names become Counter entries.  We do not want to
	 * create new counter entries for an unbounded set of names.  Use "appendToSpanInfo" to add
	 * things like the number of results returned from a method.
	 */
	public static void appendToSpanName(String text){
		TraceContext ctx = get();
		if(ctx==null || ctx.getCurrentSpan()==null){ return; }
		TraceSpan span = ctx.getCurrentSpan();
		boolean addSpace = StringTool.notEmpty(span.getName());
		span.setName(StringTool.nullSafe(span.getName()) + (addSpace ? " " : "") + text);
		Counters.inc(span.getName());//yes, this is double-counting the span
	}
	
	public static void appendToSpanInfo(String text){
		TraceContext ctx = get();
		if(ctx==null || ctx.getCurrentSpan()==null){ return; }
		TraceSpan span = ctx.getCurrentSpan();
		boolean addSpace = StringTool.notEmpty(span.getInfo());
		span.setInfo(StringTool.nullSafe(span.getInfo()) + (addSpace ? " " : "") + text);
	}
	
	public static void finishSpan(){
		TraceContext ctx = get();
		if(ctx==null || ctx.getCurrentSpan()==null){ return; }
		ctx.getCurrentSpan().markFinish();
		ctx.getSpans().add(ctx.getCurrentSpan());
		ctx.popSpanFromStack();
	}
	
	
	/*************** trace span methods ***************************************/
	
	protected TraceSpan pushSpanOntoStack(TraceSpan span){
		if(span==null){ return null; }
		this.spanStack.add(span);
		return span;
	}
	
	protected TraceSpan getCurrentSpan(){
		TraceContext ctx = get();
		if(ctx==null || CollectionTool.isEmpty(ctx.spanStack)){ return null; }
		return ctx.getSpanStack().get(this.spanStack.size()-1);
	}
	
	protected TraceSpan popSpanFromStack(){
		if(CollectionTool.isEmpty(this.spanStack)){ return null; }
		TraceSpan span = this.getCurrentSpan();
		this.spanStack.remove(this.spanStack.size()-1);
		return span;
	}
	
	
	/****************** static collectors ******************************/
	
	public static <V> void collect(TracedCallable<V> callable){
		TraceContext ctx = get();
		if(ctx==null || callable==null){ return; }
		ctx.getThreads().addAll(CollectionTool.nullSafe(callable.getThreads()));
		ctx.getSpans().addAll(CollectionTool.nullSafe(callable.getSpans()));
//		logger.warn("collected "+CollectionTool.size(ctx.getSpans())+" spans");
	}
	
	
	/******************** ThreadLocal *******************************************************/
	
	public static ThreadLocal<TraceContext> traceContext = new ThreadLocal<TraceContext>();

	public static TraceContext bindToThread(TraceContext ctx) {
		traceContext.set(ctx);
		return ctx;
	}

	public static TraceContext get() {
		if(traceContext==null){ return null; }
		return traceContext.get();
	}

	public static void clearFromThread() {
		traceContext.set(null);
	}

	
	/*********************** standard *********************************************/
	
	@Override
	public String toString(){
		return getClass().getSimpleName()+"["+currentThread.getName()+"]";
	}

	
	/*********************** get/set *********************************************/

	public TraceThread getCurrentThread(){
		return currentThread;
	}

	public void setCurrentThread(TraceThread currentThread){
		this.currentThread = currentThread;
	}

	public List<TraceThread> getThreads(){
		return threads;
	}

	public void setThreads(List<TraceThread> threads){
		this.threads = threads;
	}

	public void setTraceId(Long traceId){
		this.traceId = traceId;
	}

	public void setTraceThreadParentId(Long traceThreadParentId){
		this.traceThreadParentId = traceThreadParentId;
	}

	public Integer getNextSpanSequence(){
		return nextSpanSequence;
	}

	public void setNextSpanSequence(Integer nextSpanSequence){
		this.nextSpanSequence = nextSpanSequence;
	}

	public List<TraceSpan> getSpans(){
		return spans;
	}

	public void setSpans(List<TraceSpan> spans){
		this.spans = spans;
	}

	public String getServerName(){
		return serverName;
	}

	public void setServerName(String serverName){
		this.serverName = serverName;
	}

	public Long getTraceId(){
		return traceId;
	}

	public Long getTraceThreadParentId(){
		return traceThreadParentId;
	}

	public ArrayList<TraceSpan> getSpanStack(){
		return spanStack;
	}

	public void setSpanStack(ArrayList<TraceSpan> spanStack){
		this.spanStack = spanStack;
	}

}
