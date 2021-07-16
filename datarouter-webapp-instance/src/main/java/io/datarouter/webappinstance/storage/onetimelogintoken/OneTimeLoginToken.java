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
package io.datarouter.webappinstance.storage.onetimelogintoken;

import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.DateField;
import io.datarouter.model.field.imp.DateFieldKey;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class OneTimeLoginToken extends BaseDatabean<OneTimeLoginTokenKey,OneTimeLoginToken>{

	private final String token;
	private final String targetServerName;
	private final String targetServerIp;
	private final Date deadline;

	public OneTimeLoginToken(){
		this(null, null, null, null, null);
	}

	public OneTimeLoginToken(Long userId, String token, String targetServerName, String targetServerIp, Date deadline){
		super(new OneTimeLoginTokenKey(userId));
		this.token = token;
		this.targetServerName = targetServerName;
		this.targetServerIp = targetServerIp;
		this.deadline = deadline;
	}

	public OneTimeLoginToken(Long userId, String token, String targetServerName, Date deadline){
		this(userId, token, targetServerName, null, deadline);
	}

	public static class FieldKeys{
		public static final StringFieldKey token = new StringFieldKey("token");
		public static final StringFieldKey targetServerName = new StringFieldKey("targetServerName");
		public static final StringFieldKey targetServerIp = new StringFieldKey("targetServerIp");
		@SuppressWarnings("deprecation")
		public static final DateFieldKey deadline = new DateFieldKey("deadline");
	}

	public static class OneTimeLoginTokenFielder extends BaseDatabeanFielder<OneTimeLoginTokenKey,OneTimeLoginToken>{

		public OneTimeLoginTokenFielder(){
			super(OneTimeLoginTokenKey.class);
		}

		@SuppressWarnings("deprecation")
		@Override
		public List<Field<?>> getNonKeyFields(OneTimeLoginToken databean){
			return List.of(
					new StringField(FieldKeys.token, databean.token),
					new StringField(FieldKeys.targetServerName, databean.targetServerName),
					new StringField(FieldKeys.targetServerIp, databean.targetServerIp),
					new DateField(FieldKeys.deadline,databean.deadline));
		}
	}

	@Override
	public Supplier<OneTimeLoginTokenKey> getKeySupplier(){
		return OneTimeLoginTokenKey::new;
	}

	public String getToken(){
		return token;
	}

	public String getTargetServerName(){
		return targetServerName;
	}

	public String getTargetServerIp(){
		return targetServerIp;
	}

	public Date getDeadline(){
		return deadline;
	}

}
