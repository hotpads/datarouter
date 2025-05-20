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
package io.datarouter.auth.service.deprovisioning;

import java.util.List;

/**
 * these methods are intended to be called by {@link UserDeprovisioningService} in conjunction with
 * {@link UserDeprovisioningListener}. See {@link UserDeprovisioningService} for exact order and configuration.
 */
public interface UserDeprovisioningStrategy{

	/**
	 * for each username, removes all permissions, deletes or invalidates all sessions, and sets the user to
	 * disabled. should be fault tolerant for usernames that do not exist.
	 * @param usernames to deprovision
	 */
	void deprovisionUsers(List<String> usernames);

	/**
	 * restores previously deprovisioned users. should be fault tolerant for usernames that do not exist or are not
	 * in a restorable state.
	 * @param username to restore, which was previously deprovisioned using
	 * {@link UserDeprovisioningStrategy#deprovisionUsers(List)}
	 */
	void restoreUser(String username);

}
