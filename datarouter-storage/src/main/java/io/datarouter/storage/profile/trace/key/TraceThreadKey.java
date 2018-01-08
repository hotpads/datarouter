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
import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.key.primary.base.BaseEntityPrimaryKey;
import io.datarouter.util.number.RandomTool;

public class TraceThreadKey extends BaseEntityPrimaryKey<TraceEntityKey,TraceThreadKey>{

	private Long traceId;
	private Long id;

	public static class FieldKeys{
		public static final LongFieldKey id = new LongFieldKey("id");
	}

	@Override
	public TraceEntityKey getEntityKey(){
		return new TraceEntityKey(traceId);
	}

	@Override
	public String getEntityKeyName(){
		return null;
	}

	@Override
	public TraceThreadKey prefixFromEntityKey(TraceEntityKey entityKey){
		return new TraceThreadKey(entityKey.getTraceId(), null);
	}

	@Override
	public List<Field<?>> getPostEntityKeyFields(){
		return Arrays.asList(new LongField(FieldKeys.id, id));
	}

	/****************************** constructor ********************************/

	TraceThreadKey(){
	}

	public TraceThreadKey(Long traceId, boolean hasParent){
		this.traceId = traceId;
		if(!hasParent){
			this.id = 0L;
		}else{
			this.id = RandomTool.nextPositiveLong();
		}
	}

	public TraceThreadKey(Long traceId, Long threadId){
		this.traceId = traceId;
		this.id = threadId;
	}

	/****************************** get/set ********************************/

	public Long getTraceId(){
		return traceId;
	}

	public Long getId(){
		return id;
	}

	public void setId(Long id){
		this.id = id;
	}

}