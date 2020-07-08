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
package io.datarouter.storage.test.node.basic.map.databean;

import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.key.entity.base.BaseEntityKey;
import io.datarouter.model.key.entity.base.BaseStringEntityPartitioner;
import io.datarouter.util.HashMethods;

public class MapStorageBeanEntityKey extends BaseEntityKey<MapStorageBeanEntityKey>{

	private Long entityId;

	public static class FieldKeys{
		public static final LongFieldKey entityId = new LongFieldKey("entityId");
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(new LongField(FieldKeys.entityId, entityId));
	}

	public static class MapStorageBeanEntityPartitioner
	extends BaseStringEntityPartitioner<MapStorageBeanEntityKey>{

		public MapStorageBeanEntityPartitioner(){
			super(HashMethods::longDjbHash, 4);
		}

		@Override
		protected String makeStringHashInput(MapStorageBeanEntityKey ek){
			return String.valueOf(ek.entityId);
		}

	}

	public MapStorageBeanEntityKey(){
	}

	public MapStorageBeanEntityKey(Long entityId){
		this.entityId = entityId;
	}

	public Long getEntityId(){
		return entityId;
	}

}
