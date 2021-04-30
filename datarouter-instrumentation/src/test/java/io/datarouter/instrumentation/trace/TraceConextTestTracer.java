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

public class TraceConextTestTracer implements Tracer{

	private final W3TraceContext traceContext;

	public TraceConextTestTracer(W3TraceContext traceContext){
		this.traceContext = traceContext;
	}

	@Override
	public String getServerName(){
		return null;
	}

	@Override
	public String getTraceId(){
		return null;
	}

	@Override
	public Optional<W3TraceContext> getTraceContext(){
		return Optional.of(traceContext);
	}

	@Override
	public BlockingQueue<TraceThreadDto> getThreadQueue(){
		return null;
	}

	@Override
	public BlockingQueue<TraceSpanDto> getSpanQueue(){
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
	public void addThread(TraceThreadDto thread){
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
	public void startSpan(String name){
	}

	@Override
	public void addSpan(TraceSpanDto span){
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
	public TraceSpanDto getCurrentSpan(){
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
	public Tracer createChildTracer(){
		return null;
	}

}
