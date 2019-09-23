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
package io.datarouter.storage.node.type.index;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.unique.UniqueIndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.util.tuple.Range;

public class NoOpUniqueIndexNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends UniqueIndexEntry<IK,IE,PK,D>>
implements UniqueIndexNode<PK,D,IK,IE>{

	@Override
	public IE get(IK uniqueKey, Config config){
		return null;
	}

	@Override
	public List<IE> getMulti(Collection<IK> uniqueKeys, Config config){
		return Collections.emptyList();
	}

	@Override
	public D lookupUnique(IK indexKey, Config config){
		return null;
	}

	@Override
	public List<D> lookupMultiUnique(Collection<IK> uniqueKeys, Config config){
		return Collections.emptyList();
	}

	@Override
	public Scanner<D> scanDatabeansMulti(Collection<Range<IK>> ranges, Config config){
		return Scanner.empty();
	}

	@Override
	public Scanner<IE> scanMulti(Collection<Range<IK>> ranges, Config config){
		return Scanner.empty();
	}

	@Override
	public Scanner<IK> scanKeysMulti(Collection<Range<IK>> ranges, Config config){
		return Scanner.empty();
	}

	@Override
	public void deleteUnique(IK indexKey, Config config){
	}

	@Override
	public void deleteMultiUnique(Collection<IK> uniqueKeys, Config config){
	}

}
