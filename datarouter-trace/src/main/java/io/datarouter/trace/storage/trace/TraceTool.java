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
package io.datarouter.trace.storage.trace;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;

import io.datarouter.trace.storage.entity.BaseTraceEntityKey;
import io.datarouter.trace.storage.span.BaseTraceSpan;
import io.datarouter.trace.storage.span.BaseTraceSpanKey;
import io.datarouter.trace.storage.thread.BaseTraceThreadKey;
import io.datarouter.trace.web.AccessException;
import io.datarouter.util.UlidTool;
import io.datarouter.util.duration.DatarouterDuration;

public class TraceTool{

	public static<EK extends BaseTraceEntityKey<EK>,
			PK extends BaseTraceSpanKey<EK,PK>,
			TK extends BaseTraceThreadKey<EK,TK>,
			D extends BaseTraceSpan<EK,PK,TK,D>>
	Long totalDurationOfNonChildren(Collection<D> spans){
		return spans.stream()
				.filter(BaseTraceSpan::isTopLevel)
				.mapToLong(BaseTraceSpan::getDuration)
				.sum();
	}

	public static AccessException makeException(String traceId){
		Instant traceInstant;
		try{
			traceInstant = UlidTool.getInstant(traceId);
		}catch(RuntimeException e){
			return new AccessException(e.getMessage());
		}
		Duration duration = Duration.between(traceInstant, Instant.now());
		String durationStr = new DatarouterDuration(duration).toString();
		return new AccessException("not found (" + durationStr + " old)");
	}

}
