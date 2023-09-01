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
package io.datarouter.auth.storage.user.roleapprovals;

import java.time.Instant;
import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.codec.InstantNanoToLongFieldCodec;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.LongEncodedField;
import io.datarouter.model.field.imp.comparable.LongEncodedFieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;

public class DatarouterUserRoleApprovalKey extends BaseRegularPrimaryKey<DatarouterUserRoleApprovalKey>{

	private String username;
	private String requestedRole;
	private String approverUsername;
	private Instant approvedAt;

	public DatarouterUserRoleApprovalKey(){
	}

	public DatarouterUserRoleApprovalKey(
			String username,
			String requestedRole,
			String approverUsername,
			Instant approvedAt){
		this.username = username;
		this.requestedRole = requestedRole;
		this.approverUsername = approverUsername;
		this.approvedAt = approvedAt;
	}

	public static class FieldKeys{
		public static final StringFieldKey username = new StringFieldKey("username");
		public static final StringFieldKey requestedRole = new StringFieldKey("requestedRole");
		public static final StringFieldKey approverUsername = new StringFieldKey("approverUsername");
		public static final LongEncodedFieldKey<Instant> approvedAt =
				new LongEncodedFieldKey<>("approvedAt", new InstantNanoToLongFieldCodec());
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new StringField(FieldKeys.username, username),
				new StringField(FieldKeys.requestedRole, requestedRole),
				new StringField(FieldKeys.approverUsername, approverUsername),
				new LongEncodedField<>(FieldKeys.approvedAt, approvedAt));
	}

	public String getRequestedRole(){
		return requestedRole;
	}

	public String getApproverUsername(){
		return approverUsername;
	}

}
