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

import java.util.Map;
import java.util.function.Function;

import io.datarouter.scanner.Scanner;

public enum RoleRiskFactor{
	LOW(
	"""
		Permissions that have minimal impact on the security and functionality of the web service.
		These permissions typically involve read-only access to non-sensitive data or basic actions
		that do not pose a significant risk if misused.
	"""),
	MEDIUM(
	"""
		Permissions that have moderate impact on the security and functionality of the web service.
		These permissions may involve write access to certain data or actions that can affect the system's behavior or
		configuration.
		Care should be taken when granting these permissions.
	"""),
	HIGH(
	"""
		Permissions that have a high impact on the security and functionality of the web service.
		These permissions involve access to sensitive data, critical system functions, or actions that can modify
		important configurations.
		Only trusted and authorized individuals should have these permissions.
	"""),
	CRITICAL(
	"""
		Permissions that have a critical impact on the security and functionality of the web service.
		These permissions involve access to highly sensitive data, privileged system functions,
		or actions that can cause severe damage to the system or compromise its security.
		These permissions should be granted only to a limited number of trusted administrators.
	"""),
	MAXIMUM(
	"""
		Permissions that have the maximum impact on the security and functionality of the web service.
		These permissions involve access to the most critical and sensitive areas of the system,
		such as administrative functions, server configurations, or financial data.
		Only a small group of authorized individuals with the highest level of trust should have these permissions,
		and they should be closely monitored and audited.
	""");

	public final String description;

	public static final Map<RoleRiskFactor, String> DESCRIPTIONS_BY_RISK_FACTOR =
			Scanner.of(values()).toMap(Function.identity(), roleRiskFactor -> roleRiskFactor.description);

	RoleRiskFactor(String description){
		this.description = description;
	}

}
