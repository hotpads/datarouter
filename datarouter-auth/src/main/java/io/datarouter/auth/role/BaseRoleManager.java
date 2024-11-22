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

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.string.StringTool;

public abstract class BaseRoleManager implements RoleManager{
	private static final Logger logger = LoggerFactory.getLogger(BaseRoleManager.class);

	private static final String SUPER_GROUP_ID = "super";
	private static final String DEFAULT_GROUP_ID = "default";

	private Map<String,Set<Role>> roleGroups;

	@Override
	public final Set<Role> getRolesForGroup(String groupId){
		if(roleGroups == null){
			buildRoleGroups();
		}
		return roleGroups.getOrDefault(StringTool.nullSafe(groupId), Set.of());
	}

	@Override
	public Map<String,Set<Role>> getRoleGroupMappings(){
		if(roleGroups == null){
			buildRoleGroups();
		}
		return roleGroups;
	}

	private void buildRoleGroups(){
		Map<String,Set<Role>> configurableRoleGroups = getConfigurableRoleGroups();

		//warn about ID collisions and throw exception for missing IDs
		checkAndWarnOverride(getSuperAdminGroupId(), configurableRoleGroups);
		checkAndWarnOverride(getDefaultUserGroupId(), configurableRoleGroups);
		if(getSuperAdminGroupId().equals(getDefaultUserGroupId())){
			logger.warn("Super and default role group IDs are equal. Using default roles.");
		}

		Map<String,Set<Role>> roleGroups = configurableRoleGroups.entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, entry -> Set.copyOf(entry.getValue())));
		roleGroups.put(getSuperAdminGroupId(), Set.copyOf(getSuperAdminRoles()));
		roleGroups.put(getDefaultUserGroupId(), Set.copyOf(getDefaultRoles()));
		this.roleGroups = Collections.unmodifiableMap(roleGroups);
	}

	protected Map<String,Set<Role>> getConfigurableRoleGroups(){
		return Map.of();
	}

	protected String getSuperAdminGroupId(){
		return SUPER_GROUP_ID;
	}

	protected String getDefaultUserGroupId(){
		return DEFAULT_GROUP_ID;
	}

	private void checkAndWarnOverride(String key, Map<String,Set<Role>> configurableRoleGroups){
		Objects.requireNonNull(key, "Super and default role group IDs must be defined.");
		if(configurableRoleGroups.containsKey(key)){
			logger.warn("ConfigurableRoleGroups uses a reserved role group ID, which will be ignored. Override"
							+ " getSuperUserGroupId or getDefaultUserGroupId to use this role group ID: {}", key);
		}
	}

}
