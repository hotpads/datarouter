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

public class Trace2Dto{

	public final Traceparent traceparent;
	public final String initialParentId;
	public final String context;
	public final String type;
	public final String params;
	public final Long created;
	public final Long duration;
	public final String serviceName;
	public final Integer discardedThreadCount;
	public final Integer totalThreadCount;

	public Trace2Dto(
			Traceparent traceparent,
			String initialParentId,
			String context,
			String type,
			String params,
			Long created,
			String serviceName,
			Integer discardedThreadCount,
			Integer totalThreadCount){
		this.traceparent = traceparent;
		this.created = created;
		this.initialParentId = initialParentId;
		this.context = context;
		this.type = type;
		this.params = params;
		this.duration = System.currentTimeMillis() - created;
		this.serviceName = serviceName;
		this.discardedThreadCount = discardedThreadCount;
		this.totalThreadCount = totalThreadCount;
	}

	public Traceparent getTraceparent(){
		return traceparent;
	}

	public String getInitialParentId(){
		return initialParentId;
	}

	public String getContext(){
		return context;
	}

	public String getType(){
		return type;
	}

	public String getParams(){
		return params;
	}

	public Long getCreated(){
		return created;
	}

	public Long getDuration(){
		return duration;
	}

	public String getServiceName(){
		return serviceName;
	}

	public Integer getDiscardedThreadCount(){
		return discardedThreadCount;
	}

	public Integer getTotalThreadCount(){
		return totalThreadCount;
	}

}
