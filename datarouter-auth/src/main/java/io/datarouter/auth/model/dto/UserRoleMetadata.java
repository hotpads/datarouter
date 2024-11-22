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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import io.datarouter.auth.role.Role;
import io.datarouter.auth.role.RoleApprovalType;
import io.datarouter.scanner.Scanner;

public record UserRoleMetadata(
		Role role,
		boolean hasRole,
		Map<RoleApprovalType,RoleApprovalRequirementStatus> requirementStatusByApprovalType,
		Optional<RoleApprovalType> editorPrioritizedApprovalType,
		Optional<Boolean> editorCanRevoke,
		List<String> groupsHasWithRole){

	public Optional<String> getChangeString(UserRoleMetadata oldRoleMetaData){
		if(!Objects.equals(this.role, oldRoleMetaData.role)){
			return Optional.empty();
		}
		if(hasRole && !oldRoleMetaData.hasRole){
			return Optional.of(role + ": granted (prior approvals="
					+ oldRoleMetaData.requirementStatusByApprovalType.toString() + ")");
		}else if(!hasRole && oldRoleMetaData.hasRole()){
			return Optional.of(role + ": revoked");
		}
		return Optional.of("%1$s: approval change %2$s => %3$s"
				.formatted(
						role,
						oldRoleMetaData.requirementStatusByApprovalType.toString(),
						requirementStatusByApprovalType.toString()));
	}

	public UserRoleMetadataJsDto toJsDto(){
		return new UserRoleMetadataJsDto(
				role.persistentString(),
				role.description(),
				role.riskFactor().name(),
				role.riskFactor().description,
				hasRole,
				Scanner.of(requirementStatusByApprovalType.keySet())
						.toMap(RoleApprovalType::persistentString, requirementStatusByApprovalType::get),
				editorPrioritizedApprovalType.map(RoleApprovalType::persistentString)
						.orElse(null),
				editorCanRevoke.orElse(null),
				groupsHasWithRole);
	}

	public record UserRoleMetadataJsDto(
			String roleName,
			String roleDescription,
			String roleRiskFactor,
			String roleRiskFactorDescription,
			boolean hasRole,
			Map<String,RoleApprovalRequirementStatus> requirementStatusByApprovalType,
			String editorPrioritizedApprovalType,
			Boolean editorCanRevoke,
			List<String> groupsHasWithRole){
	}

}
