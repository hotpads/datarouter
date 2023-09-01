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
package io.datarouter.auth.storage.account.credential;

import java.util.List;

import io.datarouter.model.databean.FieldlessIndexEntry;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.key.FieldlessIndexEntryPrimaryKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;

public class DatarouterAccountCredentialByAccountNameKey
extends BaseRegularPrimaryKey<DatarouterAccountCredentialByAccountNameKey>
implements FieldlessIndexEntryPrimaryKey<
		DatarouterAccountCredentialByAccountNameKey,
		DatarouterAccountCredentialKey,
		DatarouterAccountCredential>{

	private String accountName;
	private String apiKey;

	public DatarouterAccountCredentialByAccountNameKey(){
	}

	public DatarouterAccountCredentialByAccountNameKey(String accountName, String apiKey){
		this.accountName = accountName;
		this.apiKey = apiKey;
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new StringField(DatarouterAccountCredential.FieldKeys.accountName, accountName),
				new StringField(DatarouterAccountCredentialKey.FieldKeys.apiKey, apiKey));
	}

	@Override
	public DatarouterAccountCredentialKey getTargetKey(){
		return new DatarouterAccountCredentialKey(apiKey);
	}

	@Override
	public FieldlessIndexEntry<
			DatarouterAccountCredentialByAccountNameKey,
			DatarouterAccountCredentialKey,
			DatarouterAccountCredential>
	createFromDatabean(DatarouterAccountCredential target){
		var index = new DatarouterAccountCredentialByAccountNameKey(
				target.getAccountName(),
				target.getKey().getApiKey());
		return new FieldlessIndexEntry<>(DatarouterAccountCredentialByAccountNameKey::new, index);
	}

}
