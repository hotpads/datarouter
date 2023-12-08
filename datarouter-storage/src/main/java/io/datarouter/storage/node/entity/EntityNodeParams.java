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
package io.datarouter.storage.node.entity;

import java.util.function.Supplier;

import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;

public class EntityNodeParams<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>{

	private final String nodeName;
	private final Supplier<EK> entityKeySupplier;
	private final Supplier<E> entitySupplier;
	private final String entityTableName;

	public EntityNodeParams(
			String nodeName,
			Supplier<EK> entityKeySupplier,
			Supplier<E> entitySupplier,
			String entityTableName){
		this.nodeName = nodeName;
		this.entityKeySupplier = entityKeySupplier;
		this.entitySupplier = entitySupplier;
		this.entityTableName = entityTableName;
	}

	public String getNodeName(){
		return nodeName;
	}

	public Supplier<EK> getEntityKeySupplier(){
		return entityKeySupplier;
	}

	public Supplier<E> getEntitySupplier(){
		return entitySupplier;
	}

	public String getEntityTableName(){
		return entityTableName;
	}

}
