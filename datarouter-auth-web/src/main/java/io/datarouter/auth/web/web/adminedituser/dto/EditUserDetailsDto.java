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
package io.datarouter.auth.web.web.adminedituser.dto;

import java.util.List;
import java.util.Map;

import io.datarouter.auth.model.dto.DeprovisionedUserDto;
import io.datarouter.auth.model.dto.PermissionRequestDto;
import io.datarouter.auth.model.dto.UserRoleMetadata.UserRoleMetadataJsDto;
import io.datarouter.auth.web.web.adminedituser.dto.EditUserDetailsDto.Nested.PagePermissionType;
import io.datarouter.util.todo.NestedRecordImportWorkaround;

//TODO DATAROUTER-2788
public record EditUserDetailsDto(
		PagePermissionType pagePermissionType,
		String editorUsername,
		String username,
		String id,
		String token,
		String profileLink,
		List<PermissionRequestDto> requests,
		List<DatarouterUserHistoryDto> history,
		DeprovisionedUserDto deprovisionedUserDto,
		List<UserRoleMetadataJsDto> userRoleMetadataList,
		List<String> availableAccounts,
		Map<String,Boolean> currentAccounts,
		List<String> availableZoneIds,
		String currentZoneId,
		String fullName,
		Boolean hasProfileImage,
		List<EditUserDetailDto> details,
		boolean success,
		String message){

	public static EditUserDetailsDto error(String error){
		return new EditUserDetailsDto(
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				false,
				error);
	}

	@NestedRecordImportWorkaround
	public static class Nested{
		public enum PagePermissionType{
			ADMIN,
			ROLES_ONLY,
			NONE
		}
	}

}
