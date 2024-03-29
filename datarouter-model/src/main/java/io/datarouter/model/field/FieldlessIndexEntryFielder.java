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
package io.datarouter.model.field;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.databean.FieldlessIndexEntry;
import io.datarouter.model.key.FieldlessIndexEntryPrimaryKey;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.serialize.fielder.DatabeanFielder;

public class FieldlessIndexEntryFielder<
		IK extends FieldlessIndexEntryPrimaryKey<IK,PK,D>,
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends BaseDatabeanFielder<IK,FieldlessIndexEntry<IK,PK,D>>{

	public FieldlessIndexEntryFielder(Supplier<IK> keySupplier){
		super(keySupplier);
	}

	public FieldlessIndexEntryFielder(Supplier<IK> keySupplier, DatabeanFielder<PK,D> backingNodeFielder){
		this(keySupplier);
		backingNodeFielder.getOptions().forEach(this::addOption);
	}

	@Override
	public List<Field<?>> getNonKeyFields(FieldlessIndexEntry<IK,PK,D> databean){
		return List.of();
	}

}
