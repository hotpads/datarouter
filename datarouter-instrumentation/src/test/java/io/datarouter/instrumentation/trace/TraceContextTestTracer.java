/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.instrumentation.trace;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;

public class TraceContextTestTracer implements Tracer{

	private final W3TraceContext traceContext;

	public TraceContextTestTracer(W3TraceContext traceContext){
		this.traceContext = traceContext;
	}

	@Override
	public String getServerName(){
		return null;
	}

	@Override
	public Optional<W3TraceContext> getTraceContext(){
		return Optional.of(traceContext);
	}

	@Override
	public BlockingQueue<Trace2ThreadDto> getThreadQueue(){
		return null;
	}

	@Override
	public BlockingQueue<Trace2SpanDto> getSpanQueue(){
		return null;
	}

	@Override
	public Long getCurrentThreadId(){
		return null;
	}

	@Override
	public Integer getDiscardedThreadCount(){
		return null;
	}

	@Override
	public void incrementDiscardedThreadCount(int discardedThreadCount){

	}

	@Override
	public void createThread(String name, long queueTimeMs){

	}

	@Override
	public void startThread(){
	}

	@Override
	public void addThread(Trace2ThreadDto thread){
	}

	@Override
	public void appendToThreadInfo(String text){
	}

	@Override
	public void finishThread(){
	}

	@Override
	public Integer getDiscardedSpanCount(){
		return null;
	}

	@Override
	public void startSpan(String name, TraceSpanGroupType groupType){
	}

	@Override
	public void addSpan(Trace2SpanDto span){
	}

	@Override
	public void appendToSpanInfo(String text){
	}

	@Override
	public void finishSpan(){
	}

	@Override
	public void incrementDiscardedSpanCount(int discardedSpanCount){
	}

	@Override
	public Trace2SpanDto getCurrentSpan(){
		return null;
	}

	@Override
	public boolean getForceSave(){
		return false;
	}

	@Override
	public void setForceSave(){
	}

	@Override
	public void setSaveThreadCpuTime(boolean saveThreadCpuTime){
		return;
	}

	@Override
	public void setSaveThreadMemoryAllocated(boolean saveThreadMemoryAllocated){
		return;
	}

	@Override
	public void setSaveSpanCpuTime(boolean saveSpanCpuTime){
		return;
	}

	@Override
	public void setSaveSpanMemoryAllocated(boolean saveSpanMemoryAllocated){
		return;
	}

	@Override
	public Tracer createChildTracer(){
		return null;
	}

}
