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

import java.util.Set;

import io.datarouter.enums.StringMappedEnum;

public enum DatarouterUserRole implements RoleEnum<DatarouterUserRole>{
	ADMIN("admin", "Legacy admin role. You probably want datarouterAdmin", RoleRiskFactor.HIGH),
	DATAROUTER_ACCOUNTS("datarouterAccounts", "Permission to view and edit accounts", RoleRiskFactor.HIGH),
	DATAROUTER_ADMIN("datarouterAdmin", "The highest level of permission in Datarouter, which includes at minimum all"
			+ " other base Datarouter role permissions", RoleRiskFactor.CRITICAL),
	DATAROUTER_JOB("datarouterJob", "Permission to view and manage jobs & conveyors", RoleRiskFactor.HIGH),
	DATAROUTER_MONITORING(
			"datarouterMonitoring",
			"Permission to view monitoring pages (e.g. stack traces, server status, etc)."
					+ " Can also set monitoring thresholds.",
			RoleRiskFactor.MEDIUM),
	DATAROUTER_SETTINGS("datarouterSettings", "Permission to view & edit cluster settings.", RoleRiskFactor.HIGH),
	DATAROUTER_TOOLS("datarouterTools", "Permission to use miscellaneous admin tools.", RoleRiskFactor.HIGH),
	DOC_USER("docUser", "Permission to view API and other service documentation pages.", RoleRiskFactor.LOW),
	REQUESTOR(
			"requestor",
			"Most basic permission. Only grants the ability to request other roles.",
			RoleRiskFactor.LOW),
	USER("user", "General role one step up from requestor. Provides various low-risk permissions.", RoleRiskFactor.LOW);

	public static final StringMappedEnum<DatarouterUserRole> BY_PERSISTENT_STRING
			= new StringMappedEnum<>(values(), value -> value.role.persistentString);

	private final Role role;

	DatarouterUserRole(String persistentString, String description, RoleRiskFactor riskFactor){
		this.role = new Role(persistentString, description, riskFactor);
	}

	@Override
	public String getPersistentString(){
		return role.getPersistentString();
	}

	@Override
	public DatarouterUserRole fromPersistentString(String str){
		return BY_PERSISTENT_STRING.fromOrNull(str);
	}

	@Override
	public Role getRole(){
		return role;
	}

	public static Set<Role> getDatarouterPrivilegedRoles(){
		return Set.of(
				DatarouterUserRole.ADMIN.getRole(),
				DatarouterUserRole.DATAROUTER_ACCOUNTS.getRole(),
				DatarouterUserRole.DATAROUTER_ADMIN.getRole(),
				DatarouterUserRole.DATAROUTER_JOB.getRole(),
				DatarouterUserRole.DATAROUTER_MONITORING.getRole(),
				DatarouterUserRole.DATAROUTER_SETTINGS.getRole(),
				DatarouterUserRole.DATAROUTER_TOOLS.getRole());
	}

}
