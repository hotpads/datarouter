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
package io.datarouter.storage.metric;

import io.datarouter.pathnode.PathNode;

public interface Gauges{

	void save(String key, long value);

	default void save(PathNode key, long value){
		save(key.join("", " ", ""), value);
	}

	default void save(String key, int value){
		save(key, Long.valueOf(value));
	}

	static class NoOpGauges implements Gauges{

		@Override
		public void save(String key, long value){
		}

	}

}
