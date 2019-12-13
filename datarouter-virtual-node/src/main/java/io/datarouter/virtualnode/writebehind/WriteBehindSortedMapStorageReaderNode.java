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
package io.datarouter.virtualnode.writebehind;

import java.util.Collection;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.combo.reader.SortedMapStorageReader;
import io.datarouter.util.tuple.Range;

public class WriteBehindSortedMapStorageReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends SortedMapStorageReader<PK,D>>
extends WriteBehindMapStorageReaderNode<PK,D,N>
implements SortedMapStorageReader<PK,D>{

	public WriteBehindSortedMapStorageReaderNode(Datarouter datarouter, N backingNode){
		super(datarouter, backingNode);
	}

	@Override
	public Scanner<PK> scanKeysMulti(Collection<Range<PK>> ranges, Config config){
		return backingNode.scanKeysMulti(ranges, config);
	}

	@Override
	public Scanner<D> scanMulti(Collection<Range<PK>> ranges, Config config){
		return backingNode.scanMulti(ranges, config);
	}

}
