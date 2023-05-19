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
package io.datarouter.auth.storage.roleapprovals;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.codec.InstantNanoToLongFieldCodec;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.LongEncodedField;
import io.datarouter.model.field.imp.comparable.LongEncodedFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class DatarouterUserRoleApproval extends BaseDatabean<DatarouterUserRoleApprovalKey,DatarouterUserRoleApproval>{

	private String approvalType;
	private Instant allApprovalRequirementsMetAt;

	public DatarouterUserRoleApproval(){
		super(new DatarouterUserRoleApprovalKey());
	}

	public DatarouterUserRoleApproval(
			String username,
			String requestedRole,
			String approverUsername,
			Instant approvedAt,
			String approvalType,
			Instant allApprovalRequirementsMetAt){
		super(new DatarouterUserRoleApprovalKey(username, requestedRole, approverUsername, approvedAt));
		this.approvalType = approvalType;
		this.allApprovalRequirementsMetAt = allApprovalRequirementsMetAt;
	}


	public static class FieldKeys{
		public static final StringFieldKey approvalType = new StringFieldKey("approvalType");
		public static final LongEncodedFieldKey<Instant> allApprovalRequirementsMetAt =
				new LongEncodedFieldKey<>("allApprovalRequirementsMetAt", new InstantNanoToLongFieldCodec());
	}

	public static class DatarouterUserRoleApprovalFielder
	extends BaseDatabeanFielder<DatarouterUserRoleApprovalKey,DatarouterUserRoleApproval>{

		public DatarouterUserRoleApprovalFielder(){
			super(DatarouterUserRoleApprovalKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(DatarouterUserRoleApproval databean){
			return List.of(
					new StringField(FieldKeys.approvalType, databean.approvalType),
					new LongEncodedField<>(
							FieldKeys.allApprovalRequirementsMetAt, databean.allApprovalRequirementsMetAt));
		}
	}

	@Override
	public Supplier<DatarouterUserRoleApprovalKey> getKeySupplier(){
		return DatarouterUserRoleApprovalKey::new;
	}

	public Instant getAllApprovalRequirementsMetAt(){
		return allApprovalRequirementsMetAt;
	}

	public String getApprovalType(){
		return approvalType;
	}

	public DatarouterUserRoleApproval withAllApprovalRequirementsMetAt(Instant instant){
		this.allApprovalRequirementsMetAt = instant;
		return this;
	}

}
