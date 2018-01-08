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
package io.datarouter.storage.serialize.fieldcache;

import java.util.function.Supplier;

import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.entity.EntityPartitioner;
import io.datarouter.model.key.entity.base.NoOpEntityPartitioner;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.util.lang.ReflectionTool;

public class EntityFieldInfo<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>{

	public static final byte ENTITY_PREFIX_TERMINATOR = 0;

	private String entityTableName;
	private Class<EK> entityKeyClass;
	private EK sampleEntityKey;
	private EntityPartitioner<EK> entityPartitioner;
	private Supplier<E> entitySupplier;


	public EntityFieldInfo(EntityNodeParams<EK,E> params){
		this.entityTableName = params.getEntityTableName();
		this.entityKeyClass = params.getEntityKeyClass();
		this.sampleEntityKey = ReflectionTool.create(entityKeyClass);
		Supplier<EntityPartitioner<EK>> entityPartitionerSupplier = params.getEntityPartitionerSupplier();
		if(entityPartitionerSupplier == null){
			this.entityPartitioner = new NoOpEntityPartitioner<>();
		}else{
			this.entityPartitioner = entityPartitionerSupplier.get();
		}
		this.entitySupplier = params.getEntitySupplier();
	}


	public static byte getEntityPrefixTerminator(){
		return ENTITY_PREFIX_TERMINATOR;
	}

	public String getEntityTableName(){
		return entityTableName;
	}

	public Class<EK> getEntityKeyClass(){
		return entityKeyClass;
	}

	public EK getSampleEntityKey(){
		return sampleEntityKey;
	}

	public EntityPartitioner<EK> getEntityPartitioner(){
		return entityPartitioner;
	}

	public Supplier<E> getEntitySupplier(){
		return entitySupplier;
	}
}
