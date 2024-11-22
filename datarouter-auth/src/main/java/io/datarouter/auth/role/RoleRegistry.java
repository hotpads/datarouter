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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import io.datarouter.scanner.Scanner;

public interface RoleRegistry{

	Role
			DATAROUTER_ACCOUNTS = new Role(
					"datarouterAccounts",
					"Permission to view and edit accounts",
					RoleRiskFactor.HIGH),
			DATAROUTER_ADMIN = new Role(
					"datarouterAdmin",
					"The highest level of permission in Datarouter, which includes at minimum all other base "
							+ "Datarouter role permissions",
					RoleRiskFactor.CRITICAL),
			DATAROUTER_JOB = new Role(
					"datarouterJob",
					"Permission to view and manage jobs & conveyors",
					RoleRiskFactor.HIGH),
			DATAROUTER_MONITORING = new Role(
					"datarouterMonitoring",
					"Permission to view monitoring pages (e.g. stack traces, server status, etc)."
							+ " Can also set monitoring thresholds.",
					RoleRiskFactor.MEDIUM),
			DATAROUTER_SETTINGS = new Role(
					"datarouterSettings",
					"Permission to view & edit cluster settings.",
					RoleRiskFactor.HIGH),
			DATAROUTER_TOOLS = new Role(
					"datarouterTools",
					"Permission to use miscellaneous admin tools.",
					RoleRiskFactor.HIGH),
			DOC_USER = new Role(
					"docUser",
					"Permission to view API and other service documentation pages.",
					RoleRiskFactor.LOW),
			REQUESTOR = new Role(
					"requestor",
					"Most basic permission. Only grants the ability to request other roles.",
					RoleRiskFactor.LOW),
			USER = new Role(
					"user",
					"General role one step up from requestor. Provides various low-risk permissions.",
					RoleRiskFactor.LOW);

	Set<Role> DEFAULT_ROLES = Set.of(
			DATAROUTER_ACCOUNTS,
			DATAROUTER_ADMIN,
			DATAROUTER_JOB,
			DATAROUTER_MONITORING,
			DATAROUTER_SETTINGS,
			DATAROUTER_TOOLS,
			DOC_USER,
			REQUESTOR,
			USER);

	default Set<Role> getDefaultRoles(){
		return DEFAULT_ROLES;
	}

	Set<Role> getAdditionalRoles();

	default Set<Role> getAllRoles(){
		return Scanner.of(getDefaultRoles())
				.append(getAdditionalRoles())
				.collect(HashSet::new);
	}

	default Optional<Role> findRoleFromPersistentString(String persistentString){
		return Scanner.of(getAllRoles())
				.include(role -> role.persistentString().equals(persistentString))
				.findFirst();
	}

}
