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
package io.datarouter.auth.model.dto;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import io.datarouter.scanner.Scanner;
import io.datarouter.web.user.role.Role;
import io.datarouter.web.user.role.RoleApprovalType;

public record UserRoleMetadata(
		Role role,
		boolean privilegesGranted,
		Map<RoleApprovalType,RoleApprovalRequirementStatus> requirementStatusByApprovalType,
		Optional<RoleApprovalType> editorPrioritizedApprovalType,
		Optional<Boolean> editorCanRevoke){

	public String getChangeString(UserRoleMetadata oldRoleMetaData){
		if(!Objects.equals(this.role, oldRoleMetaData.role)){
			return null;
		}
		if(privilegesGranted && !oldRoleMetaData.privilegesGranted){
			return role + ": granted (prior approvals="
					+ oldRoleMetaData.requirementStatusByApprovalType.toString() + ")";
		}else if(!privilegesGranted && oldRoleMetaData.privilegesGranted()){
			return role + ": revoked";
		}
		return "%1$s: approval change %2$s => %3$s"
				.formatted(
						role,
						oldRoleMetaData.requirementStatusByApprovalType.toString(),
						requirementStatusByApprovalType.toString());
	}

	public UserRoleMetadataJsDto toJsDto(){
		return new UserRoleMetadataJsDto(
				role.persistentString,
				role.description,
				privilegesGranted,
				Scanner.of(requirementStatusByApprovalType.keySet())
						.toMap(RoleApprovalType::persistentString, requirementStatusByApprovalType::get),
				editorPrioritizedApprovalType.map(RoleApprovalType::persistentString)
						.orElse(null),
				editorCanRevoke.orElse(null));
	}

	public record UserRoleMetadataJsDto(
			String roleName,
			String roleDescription,
			boolean privilegesGranted,
			Map<String,RoleApprovalRequirementStatus> requirementStatusByApprovalType,
			String editorPrioritizedApprovalType,
			Boolean editorCanRevoke){}
}
