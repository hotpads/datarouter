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
package io.datarouter.storage.trace.tracer;

public class TracerThreadLocal{

	/*************** ThreadLocal ***************/

	private static final ThreadLocal<Tracer> tracerThreadLocal = new ThreadLocal<>();


	/************** methods *****************/

	public static Tracer bindToThread(Tracer tracer){
		tracerThreadLocal.set(tracer);
		return tracer;
	}

	public static void clearFromThread(){
		tracerThreadLocal.set(null);
	}

	public static Tracer get(){
		return tracerThreadLocal.get();
	}

}
