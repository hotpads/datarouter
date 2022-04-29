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
package io.datarouter.storage.node.op.index;

import java.util.Collection;
import java.util.Collections;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader;
import io.datarouter.storage.util.KeyRangeTool;
import io.datarouter.util.tuple.Range;

public interface IndexReader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK,IE,PK,D>>
extends SortedStorageReader<IK,IE>{

	/*------------ scanDatabeansMulti ------------*/

	Scanner<D> scanDatabeansMulti(Collection<Range<IK>> ranges, Config config);

	default Scanner<D> scanDatabeansMulti(Collection<Range<IK>> ranges){
		return scanDatabeansMulti(ranges, new Config());
	}

	/*------------ scanDatabeans ------------*/

	default Scanner<D> scanDatabeans(Range<IK> range, Config config){
		return scanDatabeansMulti(Collections.singletonList(range), config);
	}

	default Scanner<D> scanDatabeans(Range<IK> range){
		return scanDatabeans(range, new Config());
	}

	default Scanner<D> scanDatabeans(Config config){
		return scanDatabeans(Range.everything(), config);
	}

	default Scanner<D> scanDatabeans(){
		return scanDatabeans(Range.everything(), new Config());
	}

	/*------------ scanDatabeansWithPrefix ------------*/

	default Scanner<D> scanDatabeansWithPrefix(IK prefix, Config config){
		return scanDatabeans(KeyRangeTool.forPrefix(prefix), config);
	}

	default Scanner<D> scanDatabeansWithPrefix(IK prefix){
		return scanDatabeansWithPrefix(prefix, new Config());
	}

	/*------------ scanDatabeansWithPrefixes ------------*/

	default Scanner<D> scanDatabeansWithPrefixes(Collection<IK> prefixes, Config config){
		return scanDatabeansMulti(SortedStorageReader.getRangesFromPrefixes(prefixes), config);
	}

	default Scanner<D> scanDatabeansWithPrefixes(Collection<IK> prefixes){
		return scanDatabeansWithPrefixes(prefixes, new Config());
	}

}
