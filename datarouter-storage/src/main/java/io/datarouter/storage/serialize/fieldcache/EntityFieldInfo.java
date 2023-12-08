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
package io.datarouter.storage.serialize.fieldcache;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.entity.Entity;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.storage.node.entity.EntityNodeParams;

public class EntityFieldInfo<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>{

	public static final byte ENTITY_PREFIX_TERMINATOR = 0;

	private final String entityTableName;
	private final Supplier<EK> entityKeySupplier;
	private final EK sampleEntityKey;
	private final Supplier<E> entitySupplier;
	private final List<Field<?>> entityKeyFields;

	public EntityFieldInfo(EntityNodeParams<EK,E> params){
		this.entityTableName = params.getEntityTableName();
		this.entityKeySupplier = params.getEntityKeySupplier();
		this.sampleEntityKey = entityKeySupplier.get();
		this.entitySupplier = params.getEntitySupplier();
		this.entityKeyFields = entityKeySupplier.get().getFields();
	}

	public static byte getEntityPrefixTerminator(){
		return ENTITY_PREFIX_TERMINATOR;
	}

	public String getEntityTableName(){
		return entityTableName;
	}

	public Supplier<EK> getEntityKeySupplier(){
		return entityKeySupplier;
	}

	public EK getSampleEntityKey(){
		return sampleEntityKey;
	}

	public Supplier<E> getEntitySupplier(){
		return entitySupplier;
	}

	public List<Field<?>> getEntityKeyFields(){
		return entityKeyFields;
	}

}
