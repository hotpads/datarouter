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
package io.datarouter.auth.web.deprovisioning;

public class UserDeprovisioningStatusDto{

	public static UserDeprovisioningStatusDto PROVISIONED = new UserDeprovisioningStatusDto("PROVISIONED",
			"This user is provisioned.", true, true, false);
	public static UserDeprovisioningStatusDto DEPROVISIONED = new UserDeprovisioningStatusDto("DEPROVISIONED",
			"This user is deprovisioned.", false, false, true);
	public static UserDeprovisioningStatusDto FLAGGED = new UserDeprovisioningStatusDto("FLAGGED",
			"This user is provisioned but is flagged for deprovisioning.", true, true, false);
	public static UserDeprovisioningStatusDto UNRESTORABLE = new UserDeprovisioningStatusDto("UNRESTORABLE",
			"This user is deprovisioned and in a state that cannot be restored.", false, false, false);

	public final String name;
	public final String description;
	public final boolean isUserEditable;
	public final boolean allowDeprovision;
	public final boolean allowRestore;

	private UserDeprovisioningStatusDto(String name, String description, boolean isUserEditable,
			boolean allowDeprovision, boolean allowRestore){
		this.name = name;
		this.description = description;
		this.isUserEditable = isUserEditable;
		this.allowDeprovision = allowDeprovision;
		this.allowRestore = allowRestore;
	}

}
