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

import io.datarouter.model.entity.BaseEntity;
import io.datarouter.model.key.entity.EntityKey;

public class DefaultEntity<EK extends EntityKey<EK>> extends BaseEntity<EK>{

	private DefaultEntity(EK key){
		super(key);
	}

	public static <EK extends EntityKey<EK>> Supplier<DefaultEntity<EK>> supplier(Supplier<EK> entityKeySupplier){
		return () -> new DefaultEntity<>(entityKeySupplier.get());
	}

}
