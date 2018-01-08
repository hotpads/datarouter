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
/**
 *
 */
package io.datarouter.storage.profile.trace.key;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.key.primary.base.BaseEntityPrimaryKey;
import io.datarouter.util.number.RandomTool;

public class TraceKey extends BaseEntityPrimaryKey<TraceEntityKey,TraceKey>{

	private Long id;

	public static class FieldsKeys{
		public static final LongFieldKey id = new LongFieldKey("id");
	}

	/********************** entity ************************/

	@Override
	public TraceEntityKey getEntityKey(){
		return new TraceEntityKey(id);
	}

	@Override
	public String getEntityKeyName(){
		return null;
	}

	@Override
	public TraceKey prefixFromEntityKey(TraceEntityKey entityKey){
		return new TraceKey(entityKey.getTraceId());
	}

	@Override // special override because TraceEntityKey calls the column "traceId"
	public List<Field<?>> getEntityKeyFields(){
		return Arrays.asList(new LongField(FieldsKeys.id, id));
	}

	@Override
	public List<Field<?>> getPostEntityKeyFields(){
		return Collections.emptyList();
	}

	/**************** construct ************************/

	public TraceKey(){// required no-arg
		this.id = RandomTool.nextPositiveLong();
	}

	public TraceKey(Long id){
		this.id = id;
	}

	/*************** get/set *************************/

	public Long getId(){
		return id;
	}

	public void setId(Long id){
		this.id = id;
	}

}