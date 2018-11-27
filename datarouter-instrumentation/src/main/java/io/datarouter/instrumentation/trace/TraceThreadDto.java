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

public class TraceThreadDto{

	private String traceId;
	private Long threadId;
	private Long parentId;
	private String name;
	private String info;
	private String serverId;
	private Long created;
	private Long queuedDuration;
	private Long runningDuration;

	public TraceThreadDto(String traceId, Long threadId, Long parentId, String serverId, String name, Long created){
		this.traceId = traceId;
		this.threadId = threadId;
		this.parentId = parentId;
		this.serverId = serverId;
		this.name = name;
		this.created = created;
	}

	public TraceThreadDto(String traceId, Long threadId, Long parentId, String name, String info, String serverId,
			Long created, Long queuedDuration, Long runningDuration){
		this(traceId, threadId, parentId, serverId, name, created);
		this.info = info;
		this.queuedDuration = queuedDuration;
		this.runningDuration = runningDuration;
	}

	public void markStart(){
		queuedDuration = System.currentTimeMillis() - created;
	}

	public void markFinish(){
		runningDuration = System.currentTimeMillis() - queuedDuration - created;
	}

	public String getTraceId(){
		return traceId;
	}

	public void setTraceId(String traceId){
		this.traceId = traceId;
	}

	public Long getThreadId(){
		return threadId;
	}

	public void setThreadId(Long threadId){
		this.threadId = threadId;
	}

	public Long getParentId(){
		return parentId;
	}

	public void setParentId(Long parentId){
		this.parentId = parentId;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getInfo(){
		return info;
	}

	public void setInfo(String info){
		this.info = info;
	}

	public String getServerId(){
		return serverId;
	}

	public void setServerId(String serverId){
		this.serverId = serverId;
	}

	public Long getCreated(){
		return created;
	}

	public void setCreated(Long created){
		this.created = created;
	}

	public Long getQueuedDuration(){
		return queuedDuration;
	}

	public void setQueuedDuration(Long queuedDuration){
		this.queuedDuration = queuedDuration;
	}

	public Long getRunningDuration(){
		return runningDuration;
	}

	public void setRunningDuration(Long runningDuration){
		this.runningDuration = runningDuration;
	}


}
