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
package io.datarouter.storage.node.op.index;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader;
import io.datarouter.storage.util.KeyRangeTool;
import io.datarouter.util.StreamTool;
import io.datarouter.util.tuple.Range;

public interface IndexReader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK,IE,PK,D>>
extends SortedStorageReader<IK,IE>{

	Iterable<D> scanDatabeansMulti(Collection<Range<IK>> ranges, Config config);

	default Stream<D> streamDatabeansMulti(Collection<Range<IK>> ranges, Config config){
		return StreamTool.stream(scanDatabeansMulti(ranges, config));
	}

	default Iterable<D> scanDatabeans(Range<IK> range, Config config){
		return scanDatabeansMulti(Arrays.asList(Range.nullSafe(range)), config);
	}

	default Stream<D> streamDatabeans(Range<IK> range, Config config){
		return StreamTool.stream(scanDatabeans(range, config));
	}

	default Iterable<D> scanDatabeansWithPrefix(IK prefix, Config config){
		return scanDatabeans(KeyRangeTool.forPrefix(prefix), config);
	}

	default Stream<D> streamDatabeansWithPrefix(IK prefix, Config config){
		return streamDatabeans(KeyRangeTool.forPrefix(prefix), config);
	}

	default Iterable<D> scanDatabeansWithPrefixes(Collection<IK> prefixes, Config config){
		return scanDatabeansMulti(SortedStorageReader.getRangesFromPrefixes(prefixes), config);
	}

	default Stream<D> streamDatabeansWithPrefixes(Collection<IK> prefixes, Config config){
		return streamDatabeansMulti(SortedStorageReader.getRangesFromPrefixes(prefixes), config);
	}

}
