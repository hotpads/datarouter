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
package io.datarouter.instrumentation.trace;

import org.testng.Assert;
import org.testng.annotations.Test;

public class W3TraceContextTests{

	private static final Long UNIX_TIME_MILLIS = 1605842383858L;
	private static final Long UNIX_TIME_NANO = 1605842383858L * 1_000_000;

	@Test
	public void validTraceContextTest(){
		String validTraceparent = "00-4bf92f3577b34da6a3ce929d0e0e4736-00000175e4110dbe-01";
		String validTracestate = "datarouter=00000175e4110dbe,rojo=00f067aa0ba902b7,congo=t61rcWkgMzE";

		W3TraceContext traceContext = new W3TraceContext(validTraceparent, validTracestate, UNIX_TIME_NANO);
		Traceparent traceparent = traceContext.getTraceparent();
		Assert.assertEquals(traceparent.traceId, "4bf92f3577b34da6a3ce929d0e0e4736");
		Assert.assertEquals(traceparent.parentId, traceContext.getTracestate().getLastestTracestate()
				.value());
		Assert.assertNotEquals(traceContext.getTimestampMs(), UNIX_TIME_MILLIS);
		Assert.assertEquals(traceContext.getTracestate().toString(), validTracestate);
	}

	@Test
	public void invalidTraceContextTest(){
		String invalidTraceparent = "00-4bf92f3577b34da6a3ce-00000175e4110dbe-01";
		String invalidTracestate = "datarouter=00000175e4110dbe,rojo=00f067aa0ba902b7,congo=t61rcWkgMzE";
		String newTracestate = Tracestate.TRACESTATE_DR_KEY + "=" + TraceIdTool.newParentId();

		W3TraceContext traceContext = new W3TraceContext(invalidTraceparent, invalidTracestate, UNIX_TIME_NANO);
		Assert.assertNotEquals(traceContext.getTraceparent().traceId, "4bf92f3577b34da6a3ce");
		Assert.assertEquals(traceContext.getTraceparent().parentId, traceContext.getTracestate().getLastestTracestate()
				.value());
		Assert.assertEquals(traceContext.getTraceparent().toString().length(), 55);
		Assert.assertEquals(traceContext.getTimestampMs(), UNIX_TIME_MILLIS);
		Assert.assertNotEquals(traceContext.getTracestate().toString(), invalidTracestate);
		Assert.assertEquals(traceContext.getTracestate().toString().length(), newTracestate.length());
	}

	@Test
	public void traceContextFlagValidationTest(){
		int shift = 0;
		int value = 1;
		for(TraceContextFlagMask mask : TraceContextFlagMask.values()){
			if(mask == TraceContextFlagMask.DEFAULT){
				Assert.assertEquals(mask.getByteMask(), 0);
			}else{
				Assert.assertEquals(mask.getByteMask(), value);
				shift++;
				value <<= shift;
			}
		}
	}

	@Test
	public void validateTraceFlag(){
		bindTraceContextToLocalThread("00"); // 0b00000000
		Assert.assertFalse(TracerTool.shouldSample());
		Assert.assertFalse(TracerTool.shouldLog());
		TracerTool.setForceSample(); // 0b00000001
		TracerTool.setForceLog(); // 0b00000011
		Assert.assertTrue(TracerTool.shouldSample());
		Assert.assertTrue(TracerTool.shouldLog());
		TracerThreadLocal.clearFromThread();

		bindTraceContextToLocalThread("01"); // 0b00000001
		Assert.assertTrue(TracerTool.shouldSample());
		Assert.assertFalse(TracerTool.shouldLog());
		TracerTool.setForceSample(); // 0b00000001
		TracerTool.setForceLog(); // 0b00000011
		Assert.assertTrue(TracerTool.shouldSample());
		Assert.assertTrue(TracerTool.shouldLog());
		TracerThreadLocal.clearFromThread();

		bindTraceContextToLocalThread("02"); // 0b00000010
		Assert.assertFalse(TracerTool.shouldSample());
		Assert.assertTrue(TracerTool.shouldLog());
		TracerTool.setForceSample(); // 0b00000011
		Assert.assertTrue(TracerTool.shouldSample());
		TracerThreadLocal.clearFromThread();

		bindTraceContextToLocalThread("6b"); // // 0b1101011
		Assert.assertTrue(TracerTool.shouldSample());
		Assert.assertTrue(TracerTool.shouldLog());
		TracerThreadLocal.clearFromThread();

	}

	private void bindTraceContextToLocalThread(String flagsInHextCode){
		String validTraceparentWithoutFlag = "00-4bf92f3577b34da6a3ce929d0e0e4736-00000175e4110dbe-";
		String validTracestate = "datarouter=00000175e4110dbe,rojo=00f067aa0ba902b7,congo=t61rcWkgMzE";
		String validTraceparentWithFlags = validTraceparentWithoutFlag + flagsInHextCode;
		W3TraceContext traceContext = new W3TraceContext(validTraceparentWithFlags, validTracestate, UNIX_TIME_MILLIS);
		Tracer tracer = new TraceContextTestTracer(traceContext);
		TracerThreadLocal.bindToThread(tracer);
	}

}
