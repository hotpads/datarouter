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
package io.datarouter.auth.storage.user.session;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.codec.MilliTimeFieldCodec;
import io.datarouter.model.field.imp.comparable.LongEncodedField;
import io.datarouter.model.field.imp.comparable.LongEncodedFieldKey;
import io.datarouter.types.MilliTime;

public abstract class BaseDatarouterSessionDatabean<
		PK extends BaseDatarouterSessionDatabeanKey<PK>,
		D extends BaseDatarouterSessionDatabean<PK,D>>
extends BaseDatabean<PK,D>{

	private MilliTime createdAt;//track how old the session is
	private MilliTime updatedAt;//last heartbeat time

	public static class FieldKeys{
		public static final LongEncodedFieldKey<MilliTime> createdAt = new LongEncodedFieldKey<>(
				"createdAt",
				new MilliTimeFieldCodec());
		public static final LongEncodedFieldKey<MilliTime> updatedAt = new LongEncodedFieldKey<>(
				"updatedAt",
				new MilliTimeFieldCodec());
	}

	public List<Field<?>> getNonKeyFields(){
		return List.of(
				new LongEncodedField<>(FieldKeys.createdAt, createdAt),
				new LongEncodedField<>(FieldKeys.updatedAt, updatedAt));
	}

	protected BaseDatarouterSessionDatabean(PK key){
		super(key);
		this.updatedAt = MilliTime.now();
	}

	public Date getUpdated(){
		return Optional.ofNullable(updatedAt)
				.map(MilliTime::toDate)
				.orElse(null);
	}

	public void setUpdated(Date updated){
		this.updatedAt = MilliTime.of(updated);
	}

	public Date getCreated(){
		return Optional.ofNullable(createdAt)
				.map(MilliTime::toDate)
				.orElse(null);
	}

	public void setCreated(Date created){
		this.createdAt = MilliTime.of(created);
	}

}
