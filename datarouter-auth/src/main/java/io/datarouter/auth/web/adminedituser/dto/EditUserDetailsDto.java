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
package io.datarouter.auth.web.adminedituser.dto;

import java.time.ZoneId;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import io.datarouter.auth.model.dto.UserRoleMetadata;
import io.datarouter.auth.model.dto.UserRoleMetadata.UserRoleMetadataJsDto;
import io.datarouter.auth.web.DatarouterPermissionRequestHandler.PermissionRequestDto;
import io.datarouter.auth.web.deprovisioning.DeprovisionedUserDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.time.ZoneIds;

public class EditUserDetailsDto{
	public enum PagePermissionType{
		ADMIN,
		ROLES_ONLY,
		NONE
	}

	public final PagePermissionType pagePermissionType;
	public final String editorUsername;
	public final String username;
	public final String id;
	public final String token;
	public final String profileLink;
	public final List<PermissionRequestDto> requests;
	public final List<DatarouterUserHistoryDto> history;
	public final DeprovisionedUserDto deprovisionedUserDto;
	public final List<UserRoleMetadataJsDto> userRoleMetadataList;
	public final List<String> availableAccounts;
	public final Map<String,Boolean> currentAccounts;
	public final List<String> availableZoneIds;
	public final String currentZoneId;
	public final String fullName;
	public final Boolean hasProfileImage;
	public final List<EditUserDetailDto> details;

	//TODO DATAROUTER-2788
	public final boolean success;
	public final String message;

	public EditUserDetailsDto(
			PagePermissionType pagePermissionType,
			String editorUsername,
			String username,
			String id,
			String token,
			String profileLink,
			List<PermissionRequestDto> requests,
			List<DatarouterUserHistoryDto> history,
			DeprovisionedUserDto deprovisionedUserDto,
			List<UserRoleMetadata> userRoleMetadataList,
			Collection<String> availableAccounts,
			Collection<String> currentAccounts,
			boolean success,
			String message,
			String currentZoneId,
			String fullName,
			boolean hasProfileImage,
			List<EditUserDetailDto> details){
		this.pagePermissionType = pagePermissionType;
		this.editorUsername = editorUsername;
		this.username = username;
		this.id = id;
		this.token = token;
		this.profileLink = profileLink;
		this.requests = requests;
		this.history = history;
		this.deprovisionedUserDto = deprovisionedUserDto;
		this.userRoleMetadataList = Scanner.of(userRoleMetadataList).map(UserRoleMetadata::toJsDto).list();
		this.availableAccounts = Scanner.of(availableAccounts)
				.sort(StringTool.COLLATOR_COMPARATOR)
				.deduplicateConsecutive()
				.list();
		Set<String> currentAccountsSet = new HashSet<>(currentAccounts);
		this.currentAccounts = Scanner.of(availableAccounts)
				.toMap(Function.identity(), currentAccountsSet::contains);
		this.success = success;
		this.message = message;
		this.availableZoneIds = Scanner.of(ZoneIds.ZONE_IDS)
				.map(ZoneId::getId)
				.sort()
				.list();
		this.currentZoneId = currentZoneId;
		this.fullName = fullName;
		this.hasProfileImage = hasProfileImage;
		this.details = details;
	}

	public EditUserDetailsDto(String errorMessage){
		this.pagePermissionType = PagePermissionType.NONE;
		this.editorUsername = null;
		this.username = null;
		this.id = null;
		this.token = null;
		this.profileLink = null;
		this.requests = null;
		this.history = null;
		this.deprovisionedUserDto = null;
		this.userRoleMetadataList = null;
		this.availableAccounts = null;
		this.currentAccounts = null;
		this.success = false;
		this.message = errorMessage;
		this.availableZoneIds = null;
		this.currentZoneId = null;
		this.fullName = null;
		this.hasProfileImage = null;
		this.details = null;
	}
}
