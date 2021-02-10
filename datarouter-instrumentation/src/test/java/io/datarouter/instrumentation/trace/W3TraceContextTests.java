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

import org.testng.Assert;
import org.testng.annotations.Test;

public class W3TraceContextTests{

	private static final Long UNIX_TIME_MILLIS = 1605842383858L;

	@Test
	public void validTraceContextTest(){
		String validTraceparent = "00-4bf92f3577b34da6a3ce929d0e0e4736-00000175e4110dbe-01";
		String validTracestate = "datarouter=00000175e4110dbe,rojo=00f067aa0ba902b7,congo=t61rcWkgMzE";

		W3TraceContext traceContext = new W3TraceContext(validTraceparent, validTracestate, UNIX_TIME_MILLIS);
		Traceparent traceparent = traceContext.getTraceparent();
		Assert.assertEquals(traceparent.traceId, "4bf92f3577b34da6a3ce929d0e0e4736");
		Assert.assertEquals(traceparent.parentId, traceContext.getTracestate().getLastestTracestate()
				.value);
		Assert.assertNotEquals(traceContext.getTimestamp().get(), UNIX_TIME_MILLIS);
		Assert.assertEquals(traceContext.getTracestate().toString(), validTracestate);
	}

	@Test
	public void inValidTraceContextTest(){
		String invalidTraceparent = "00-4bf92f3577b34da6a3ce-00000175e4110dbe-01";
		String invalidTracestate = "datarouter=00000175e4110dbe,rojo=00f067aa0ba902b7,congo=t61rcWkgMzE";
		String newTracestate = Tracestate.TRACESTATE_DR_KEY + "=" + Traceparent.createNewParentId();

		W3TraceContext traceContext = new W3TraceContext(invalidTraceparent, invalidTracestate, UNIX_TIME_MILLIS);
		Assert.assertNotEquals(traceContext.getTraceparent().traceId, "4bf92f3577b34da6a3ce");
		Assert.assertEquals(traceContext.getTraceparent().parentId, traceContext.getTracestate().getLastestTracestate()
				.value);
		Assert.assertEquals(traceContext.getTraceparent().toString().length(), 55);
		Assert.assertEquals(traceContext.getTimestamp().get(), UNIX_TIME_MILLIS);
		Assert.assertNotEquals(traceContext.getTracestate().toString(), invalidTracestate);
		Assert.assertEquals(traceContext.getTracestate().toString().length(), newTracestate.length());
	}

}
