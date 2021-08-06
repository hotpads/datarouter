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
package io.datarouter.model.index.unique.base;

import java.util.List;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.unique.UniqueKeyIndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.lang.ReflectionTool;

public abstract class BaseUniqueKeyIndexEntry<
		IK extends PrimaryKey<IK>,
		IE extends Databean<IK,IE>,
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends BaseDatabean<IK,IE>
implements UniqueKeyIndexEntry<IK,IE,PK,D>{

	public BaseUniqueKeyIndexEntry(IK key){
		super(key);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<IE> createFromDatabean(D target){
		BaseUniqueKeyIndexEntry<IK,IE,PK,D> indexEntryBuilder = ReflectionTool.create(getClass());
		IE indexEntry = indexEntryBuilder.fromPrimaryKey(target.getKey());
		return Scanner.ofNullable(indexEntry).list();
	}

}
