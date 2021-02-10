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
package io.datarouter.trace.storage.trace;

import java.util.List;

import io.datarouter.instrumentation.trace.Traceparent;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.key.primary.base.BaseEntityPrimaryKey;
import io.datarouter.trace.storage.entity.Trace2EntityKey;

public class Trace2Key
extends BaseEntityPrimaryKey<Trace2EntityKey,Trace2Key>{

	private Trace2EntityKey entityKey;
	private String parentId;

	public static class FieldKeys{
		public static final StringFieldKey parentId = new StringFieldKey("parentId");
	}


	public Trace2Key(){
		this.entityKey = new Trace2EntityKey();
	}

	public Trace2Key(Trace2EntityKey entityKey){
		this.entityKey = entityKey;
	}

	public Trace2Key(Traceparent traceparent){
		this.entityKey = new Trace2EntityKey(traceparent);
		this.parentId = traceparent.parentId;
	}

	@Override
	public Trace2Key prefixFromEntityKey(Trace2EntityKey entityKey){
		return new Trace2Key(entityKey);
	}

	@Override
	public Trace2EntityKey getEntityKey(){
		return entityKey;
	}

	@Override
	public List<Field<?>> getPostEntityKeyFields(){
		return List.of(new StringField(FieldKeys.parentId, parentId));
	}

	public String getParentId(){
		return parentId;
	}

}
