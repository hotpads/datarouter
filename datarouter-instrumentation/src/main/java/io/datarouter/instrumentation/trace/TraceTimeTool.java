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

import java.time.Instant;

public class TraceTimeTool{

	public static long epochNano(Instant instant){
		return instant.getEpochSecond() * 1_000_000_000 + instant.getNano();
	}

	public static long epochNano(){
		// System.nanoTime() doesn't start at 1970
		return epochNano(Instant.now());
	}

}
