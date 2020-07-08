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
package io.datarouter.loadtest.service;

import io.datarouter.loadtest.storage.RandomValue;
import io.datarouter.loadtest.storage.RandomValueKey;
import io.datarouter.storage.client.imp.noop.NoOpNode;
import io.datarouter.storage.node.op.raw.read.MapStorageReader;

public interface LoadTestGetDao{

	MapStorageReader<RandomValueKey,RandomValue> getReaderNode();

	class NoOpLoadTestGetDao implements LoadTestGetDao{

		@Override
		public MapStorageReader<RandomValueKey,RandomValue> getReaderNode(){
			return new NoOpNode<>();
		}

	}

}
