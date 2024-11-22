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
package io.datarouter.auth.role;

import io.datarouter.enums.StringMappedEnum;

public enum DatarouterRoleApprovalType implements RoleApprovalTypeEnum<DatarouterRoleApprovalType>{
	ADMIN("admin", 100),
	// The role cannot be granted via the UI and must be provisioned via a role group or database change
	PROHIBITED("prohibited", 101);

	public static final StringMappedEnum<DatarouterRoleApprovalType> BY_PERSISTENT_STRING
			= new StringMappedEnum<>(values(), DatarouterRoleApprovalType::getPersistentString);

	private final RoleApprovalType roleApprovalType;

	DatarouterRoleApprovalType(String persistentString, int priority){
		this.roleApprovalType = new RoleApprovalType(persistentString, priority);
	}

	@Override
	public RoleApprovalType getRoleApprovalType(){
		return roleApprovalType;
	}

	@Override
	public String getPersistentString(){
		return roleApprovalType.persistentString();
	}

	@Override
	public DatarouterRoleApprovalType fromPersistentString(String str){
		return BY_PERSISTENT_STRING.fromOrNull(str);
	}

	@Override
	public int getPriority(){
		return roleApprovalType.priority();
	}

}
