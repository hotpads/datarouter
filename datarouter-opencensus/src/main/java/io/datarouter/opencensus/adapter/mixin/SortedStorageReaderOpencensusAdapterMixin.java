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
package io.datarouter.opencensus.adapter.mixin;

import java.util.Collection;
import java.util.Optional;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.opencensus.adapter.OpencensusAdapter;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader.SortedStorageReaderNode;
import io.datarouter.util.tuple.Range;
import io.opencensus.common.Scope;

public interface SortedStorageReaderOpencensusAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends SortedStorageReaderNode<PK,D,F>>
extends SortedStorageReader<PK,D>, OpencensusAdapter{

	N getBackingNode();

	@Override
	default Scanner<PK> scanKeys(Range<PK> range, Config config){
		Optional<Scope> span = startSpan();
		try{
			return getBackingNode().scanKeys(range, config);
		}finally{
			span.ifPresent(Scope::close);
		}
	}

	@Override
	default Scanner<PK> scanRangesKeys(Collection<Range<PK>> ranges, Config config){
		Optional<Scope> span = startSpan();
		try{
			return getBackingNode().scanRangesKeys(ranges, config);
		}finally{
			span.ifPresent(Scope::close);
		}
	}

	@Override
	default Scanner<D> scan(Range<PK> range, Config config){
		Optional<Scope> span = startSpan();
		try{
			return getBackingNode().scan(range, config);
		}finally{
			span.ifPresent(Scope::close);
		}
	}

	@Override
	default Scanner<D> scanRanges(Collection<Range<PK>> ranges, Config config){
		Optional<Scope> span = startSpan();
		try{
			return getBackingNode().scanRanges(ranges, config);
		}finally{
			span.ifPresent(Scope::close);
		}
	}

}
