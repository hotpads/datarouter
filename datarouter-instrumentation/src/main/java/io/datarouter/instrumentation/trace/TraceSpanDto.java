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

public class TraceSpanDto{

	private String traceId;
	private Long threadId;
	private Integer sequence;
	private Integer parentSequence;
	private String name;
	private String info;
	private Long created;
	private Long duration;
	private Long ended;

	public TraceSpanDto(String traceId, Long threadId, Integer sequence, Integer parentSequence, Long created){
		this.traceId = traceId;
		this.threadId = threadId;
		this.sequence = sequence;
		this.parentSequence = parentSequence;
		this.created = created;
	}

	public TraceSpanDto(
			String traceId,
			Long threadId,
			Integer sequence,
			Integer parentSequence,
			String name,
			String info,
			Long created,
			Long duration){
		this(traceId, threadId, sequence, parentSequence, created);
		this.name = name;
		this.info = info;
		this.duration = duration;
	}

	public void markFinish(){
		this.ended = Trace2Dto.getCurrentTimeInNs();
		this.duration = this.ended - this.created;
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

	public Integer getSequence(){
		return sequence;
	}

	public void setSequence(Integer sequence){
		this.sequence = sequence;
	}

	public Integer getParentSequence(){
		return parentSequence;
	}

	public Integer getParentSequenceOrMinusOne(){
		return Optional.ofNullable(parentSequence).orElse(-1);
	}

	public void setParentSequence(Integer parentSequence){
		this.parentSequence = parentSequence;
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

	public Long getCreated(){
		return created;
	}

	public Long getCreatedMs(){
		return Trace2Dto.convertToMsFromNsIfNecessary(created, created);
	}

	public void setCreated(Long created){
		this.created = created;
	}

	public Long getDuration(){
		return duration;
	}

	public Long getDurationMs(){
		return Trace2Dto.convertToMsFromNsIfNecessary(duration, created);
	}

	public void setDuration(Long duration){
		this.duration = duration;
	}

	public Long getEnded(){
		return ended;
	}

}
