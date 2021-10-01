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

import java.util.Collection;
import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.unique.UniqueIndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;

public interface UniqueIndexReader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends UniqueIndexEntry<IK,IE,PK,D>>
extends IndexReader<PK,D,IK,IE>{

	String OP_lookupUnique = "lookupUnique";
	String OP_lookupMultiUnique = "lookupMultiUnique";

	IE get(IK uniqueKey, Config config);
	default IE get(IK uniqueKey){
		return get(uniqueKey, new Config());
	}

	Scanner<IE> scanMulti(Collection<IK> uniqueKeys, Config config);
	default Scanner<IE> scanMulti(Collection<IK> uniqueKeys){
		return scanMulti(uniqueKeys, new Config());
	}

	List<IE> getMulti(Collection<IK> uniqueKeys, Config config);
	default List<IE> getMulti(Collection<IK> uniqueKeys){
		return getMulti(uniqueKeys, new Config());
	}

	D lookupUnique(IK indexKey, Config config);
	default D lookupUnique(IK indexKey){
		return lookupUnique(indexKey, new Config());
	}

	Scanner<D> scanLookupMultiUnique(Collection<IK> uniqueKeys, Config config);
	default Scanner<D> scanLookupMultiUnique(Collection<IK> uniqueKeys){
		return scanLookupMultiUnique(uniqueKeys, new Config());
	}

	List<D> lookupMultiUnique(Collection<IK> uniqueKeys, Config config);
	default List<D> lookupMultiUnique(Collection<IK> uniqueKeys){
		return lookupMultiUnique(uniqueKeys, new Config());
	}

}
