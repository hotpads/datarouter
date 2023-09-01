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

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.DateField;
import io.datarouter.model.field.imp.DateFieldKey;

public abstract class BaseDatarouterSessionDatabean<
		PK extends BaseDatarouterSessionDatabeanKey<PK>,
		D extends BaseDatarouterSessionDatabean<PK,D>>
extends BaseDatabean<PK,D>{

	private Date created;//track how old the session is
	private Date updated;//last heartbeat time

	public static class FieldKeys{
		@SuppressWarnings("deprecation")
		public static final DateFieldKey created = new DateFieldKey("created");
		@SuppressWarnings("deprecation")
		public static final DateFieldKey updated = new DateFieldKey("updated");
	}

	@SuppressWarnings("deprecation")
	public List<Field<?>> getNonKeyFields(){
		return List.of(
				new DateField(FieldKeys.created, created),
				new DateField(FieldKeys.updated, updated));
	}

	protected BaseDatarouterSessionDatabean(PK key){
		super(key);
		this.updated = new Date();
	}

	public Date getUpdated(){
		return updated;
	}

	public void setUpdated(Date updated){
		this.updated = updated;
	}

	public Date getCreated(){
		return created;
	}

	public void setCreated(Date created){
		this.created = created;
	}

}
