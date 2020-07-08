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
import io.datarouter.model.field.imp.positive.UInt63Field;
import io.datarouter.model.key.primary.base.BaseEntityPrimaryKey;

public class MapStorageBeanKey extends BaseEntityPrimaryKey<MapStorageBeanEntityKey,MapStorageBeanKey>{

	private final MapStorageBeanEntityKey entityKey;
	private final Long id;

	public static class FieldKeys{
		public static final LongFieldKey id = new LongFieldKey("id");
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(new LongField(FieldKeys.id, id));
	}

	public MapStorageBeanKey(){
		this(new MapStorageBeanEntityKey(UInt63Field.nextPositiveRandom()), UInt63Field.nextPositiveRandom());
	}

	public MapStorageBeanKey(MapStorageBeanEntityKey entityKey, Long id){
		this.entityKey = entityKey;
		this.id = id;
	}

	@Override
	public MapStorageBeanKey prefixFromEntityKey(MapStorageBeanEntityKey entityKey){
		return new MapStorageBeanKey(entityKey, null);
	}

	@Override
	public List<Field<?>> getPostEntityKeyFields(){
		return List.of(new LongField(FieldKeys.id, id));
	}

	@Override
	public MapStorageBeanEntityKey getEntityKey(){
		return entityKey;
	}

	public Long getId(){
		return id;
	}

}
