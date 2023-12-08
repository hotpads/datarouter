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
package io.datarouter.trace;

import java.time.Duration;
import java.time.Instant;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.instrumentation.trace.TraceTimeTool;
import io.datarouter.instrumentation.trace.Traceparent;
import io.datarouter.trace.filter.TraceFilter;

public class TraceFilterTests{

	@Test
	public void testIsTraceIdTimestampOutsideCutoffTimes(){
		Instant currentTime = Instant.now();
		Instant futureTime = currentTime.plus(Duration.ofDays(1));
		Instant pastTime = currentTime.minus(Duration.ofDays(2));
		var traceparentCurrent = Traceparent.generateNew(TraceTimeTool.epochNano(currentTime));
		var traceparentFuture = Traceparent.generateNew(TraceTimeTool.epochNano(futureTime));
		var traceparentPast = Traceparent.generateNew(TraceTimeTool.epochNano(pastTime));

		Assert.assertFalse(TraceFilter.isTraceIdTimestampOutsideCutoffTimes(traceparentCurrent));
		Assert.assertTrue(TraceFilter.isTraceIdTimestampOutsideCutoffTimes(traceparentFuture));
		Assert.assertTrue(TraceFilter.isTraceIdTimestampOutsideCutoffTimes(traceparentPast));
	}

}
