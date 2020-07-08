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
package io.datarouter.auth.storage.useraccountmap;

import java.util.List;

import io.datarouter.auth.storage.account.DatarouterAccountKey;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.positive.UInt63Field;
import io.datarouter.model.field.imp.positive.UInt63FieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;
import io.datarouter.web.user.databean.DatarouterUserKey;

public class DatarouterUserAccountMapKey extends BaseRegularPrimaryKey<DatarouterUserAccountMapKey>{

	private Long userId;
	private String accountName;

	public DatarouterUserAccountMapKey(){
	}

	public DatarouterUserAccountMapKey(Long userId, String accountName){
		this.userId = userId;
		this.accountName = accountName;
	}

	private static class FieldKeys{
		private static final UInt63FieldKey userId = new UInt63FieldKey("userId");
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new UInt63Field(FieldKeys.userId, userId),
				new StringField(DatarouterAccountKey.FieldKeys.accountName, accountName));
	}

	public DatarouterUserKey getDatarouterUserKey(){
		return new DatarouterUserKey(userId);
	}

	public DatarouterAccountKey getDatarouterAccountKey(){
		return new DatarouterAccountKey(accountName);
	}

}
