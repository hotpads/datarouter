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
package io.datarouter.storage.node.adapter.callsite.mixin;

import java.util.Collection;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.adapter.callsite.CallsiteAdapter;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader.SortedStorageReaderNode;
import io.datarouter.util.lang.LineOfCode;
import io.datarouter.util.tuple.Range;

public interface SortedStorageReaderCallsiteAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends SortedStorageReaderNode<PK,D,F>>
extends SortedStorageReader<PK,D>, CallsiteAdapter{

	N getBackingNode();

	@Override
	default Scanner<PK> scanKeys(Range<PK> range, Config config){
		LineOfCode lineOfCode = getCallsite();
		long startNs = System.nanoTime();
		try{
			return getBackingNode().scanKeys(range, config);
		}finally{
			recordCallsite(lineOfCode, startNs, 1);
		}
	}

	@Override
	default Scanner<PK> scanKeysMulti(Collection<Range<PK>> ranges, Config config){
		LineOfCode lineOfCode = getCallsite();
		long startNs = System.nanoTime();
		try{
			return getBackingNode().scanKeysMulti(ranges, config);
		}finally{
			recordCallsite(lineOfCode, startNs, 1);
		}
	}

	@Override
	default Scanner<D> scan(Range<PK> range, Config config){
		LineOfCode lineOfCode = getCallsite();
		long startNs = System.nanoTime();
		try{
			return getBackingNode().scan(range, config);
		}finally{
			recordCallsite(lineOfCode, startNs, 1);
		}
	}

	@Override
	default Scanner<D> scanMulti(Collection<Range<PK>> ranges, Config config){
		LineOfCode lineOfCode = getCallsite();
		long startNs = System.nanoTime();
		try{
			return getBackingNode().scanMulti(ranges, config);
		}finally{
			recordCallsite(lineOfCode, startNs, 1);
		}
	}

}
