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

public class TraceDto{

	public final Long traceId;
	public final String context;
	public final String type;
	public final String params;
	public final Long created;
	public final Long duration;

	public TraceDto(Long traceId, String context, String type, String params, Long created, Long duration){
		this.traceId = traceId;
		this.context = context;
		this.type = type;
		this.params = params;
		this.created = created;
		this.duration = duration;
	}

}