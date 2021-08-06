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
package io.datarouter.auth.storage.accountpermission;

import java.util.List;

import io.datarouter.auth.storage.account.DatarouterAccountKey;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;

public class DatarouterAccountPermissionKey extends BaseRegularPrimaryKey<DatarouterAccountPermissionKey>{

	public static final String ALL_ENDPOINTS = "all";

	private String accountName;
	private String endpoint;

	private static class FieldKeys{
		private static final StringFieldKey endpoint = new StringFieldKey("endpoint");
	}

	public DatarouterAccountPermissionKey(){
	}

	public DatarouterAccountPermissionKey(String accountName){
		this(accountName, null);
	}

	public DatarouterAccountPermissionKey(String accountName, String endpoint){
		this.accountName = accountName;
		this.endpoint = endpoint;
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new StringField(DatarouterAccountKey.FieldKeys.accountName, accountName),
				new StringField(FieldKeys.endpoint, endpoint));
	}

	public DatarouterAccountPermissionKey getAccountPrefix(){
		return new DatarouterAccountPermissionKey(accountName);
	}

	public String getAccountName(){
		return accountName;
	}

	public String getEndpoint(){
		return endpoint;
	}

}
