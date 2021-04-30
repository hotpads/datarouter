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

import io.datarouter.instrumentation.exception.HttpRequestRecordDto;

public class Trace2BundleAndHttpRequestRecordDto{

	public final Trace2BundleDto traceBundleDto;
	public final HttpRequestRecordDto httpRequestRecord;

	public Trace2BundleAndHttpRequestRecordDto(Trace2BundleDto traceBundleDto, HttpRequestRecordDto httpRequestRecord){
		this.traceBundleDto = traceBundleDto;
		this.httpRequestRecord = httpRequestRecord;
	}

	public Traceparent getTraceparent(){
		if(traceBundleDto.traceDto != null){
			return traceBundleDto.traceDto.traceparent;
		}
		return traceBundleDto.traceSpanDtos.stream()
				.findAny()
				.map(spanDto -> spanDto.traceparent)
				.orElse(null);
	}

}
