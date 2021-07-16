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
package io.datarouter.model.databean;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.index.unique.UniqueIndexEntry;
import io.datarouter.model.key.FieldlessIndexEntryPrimaryKey;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.util.lang.ReflectionTool;

public class FieldlessIndexEntry<
		IK extends FieldlessIndexEntryPrimaryKey<IK,PK,D>,
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends BaseDatabean<IK,FieldlessIndexEntry<IK,PK,D>>
implements UniqueIndexEntry<IK,FieldlessIndexEntry<IK,PK,D>,PK,D>{

	private final Class<IK> keyClass;

	public FieldlessIndexEntry(Class<IK> keyClass){
		this(keyClass, ReflectionTool.create(keyClass));
	}

	public FieldlessIndexEntry(Class<IK> keyClass, IK key){
		super(key);
		this.keyClass = keyClass;
	}

	@Override
	public Supplier<IK> getKeySupplier(){
		return ReflectionTool.supplier(keyClass);
	}

	@Override
	public PK getTargetKey(){
		return getKey().getTargetKey();
	}

	@Override
	public List<FieldlessIndexEntry<IK,PK,D>> createFromDatabean(D target){
		return List.of(getKey().createFromDatabean(target));
	}

}
