/*
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
package io.datarouter.opencensus.adapter;

import java.util.Optional;

import io.datarouter.instrumentation.trace.Tracer;
import io.datarouter.instrumentation.trace.TracerThreadLocal;
import io.datarouter.util.tracer.DatarouterTracer;
import io.opencensus.common.Scope;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Span;
import io.opencensus.trace.Tracing;

public class DatarouterOpencensusTool{

	public static final String TRACEPARENT_ATTRIBUTE_KEY = "datarouterTraceparent";
	public static final String THREAD_ID_ATTRIBUTE_KEY = "datarouterThreadId";
	public static final String SEQUENCE_PARENT_ATTRIBUTE_KEY = "datarouterSequenceParent";

	public static Optional<Scope> createOpencensusSpan(){
		Tracer tracer = TracerThreadLocal.get();
		if(!(tracer instanceof DatarouterTracer)){
			return Optional.empty();
		}
		Span span = Tracing.getTracer().spanBuilderWithExplicitParent("datarouter binding span", null).startSpan();
		Scope scope = Tracing.getTracer().withSpan(span);
		String traceparent = tracer.getTraceContext().get().getTraceparent().toString();
		span.putAttribute(TRACEPARENT_ATTRIBUTE_KEY, AttributeValue.stringAttributeValue(traceparent));
		span.putAttribute(THREAD_ID_ATTRIBUTE_KEY, AttributeValue.longAttributeValue(tracer.getCurrentThreadId()));
		span.putAttribute(SEQUENCE_PARENT_ATTRIBUTE_KEY, AttributeValue.longAttributeValue(tracer.getCurrentSpan()
				.getSequence()));
		return Optional.of(scope);
	}

}
