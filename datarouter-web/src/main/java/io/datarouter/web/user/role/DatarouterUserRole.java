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
package io.datarouter.web.user.role;

import io.datarouter.enums.StringMappedEnum;

public enum DatarouterUserRole implements RoleEnum<DatarouterUserRole>{
	ADMIN("admin"),
	API_USER("apiUser"),
	DATAROUTER_ACCOUNTS("datarouterAccounts"),
	DATAROUTER_ADMIN("datarouterAdmin"),
	DATAROUTER_JOB("datarouterJob"),
	DATAROUTER_MONITORING("datarouterMonitoring"),
	DATAROUTER_SETTINGS("datarouterSettings"),
	DATAROUTER_TOOLS("datarouterTools"),
	DOC_USER("docUser"),
	REQUESTOR("requestor"),
	USER("user");

	public static final StringMappedEnum<DatarouterUserRole> BY_PERSISTENT_STRING
			= new StringMappedEnum<>(values(), value -> value.role.persistentString);

	private final Role role;

	DatarouterUserRole(String persistentString){
		this.role = new Role(persistentString);
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

}
