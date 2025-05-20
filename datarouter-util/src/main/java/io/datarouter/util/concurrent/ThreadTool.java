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
package io.datarouter.util.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.trace.TracerTool;

public class ThreadTool{
	private static final Logger logger = LoggerFactory.getLogger(ThreadTool.class);

	public static void sleep(long ms) throws InterruptedException{
		if(ms <= 0){//sleep errors on negatives
			return;
		}
		try(var _ = TracerTool.startSpanNoGroupType("sleep " + ms)){
			Thread.sleep(ms);
		}
	}

	public static void sleepUnchecked(long ms){
		if(ms <= 0){//sleep errors on negatives
			return;
		}
		try(var _ = TracerTool.startSpanNoGroupType("sleep " + ms)){
			Thread.sleep(ms);
		}catch(InterruptedException e){
			throw new UncheckedInterruptedException(e);
		}
	}

	public static void trySleep(long ms){
		if(ms <= 0){//sleep errors on negatives
			return;
		}
		try(var _ = TracerTool.startSpanNoGroupType("sleep " + ms)){
			Thread.sleep(ms);
		}catch(InterruptedException e){
			logger.warn("sleep interrupted, continuing");
		}
	}

}
