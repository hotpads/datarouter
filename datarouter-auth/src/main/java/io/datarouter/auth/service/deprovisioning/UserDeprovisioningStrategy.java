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
import java.util.Optional;

/**
 * these methods are intended to be called by {@link UserDeprovisioningService} in conjunction with
 * {@link UserDeprovisioningListener}. See {@link UserDeprovisioningService} for exact order and configuration.
 */
public interface UserDeprovisioningStrategy{

	/**
	 * flag users for later manual deprovisioning. should be fault tolerant for usernames that do not exist.
	 * @param usernames to flag
	 * @param editorUsername (if manually triggered)
	 * @return returns the successfully flagged usernames
	 */
	List<String> flagUsers(List<String> usernames, Optional<String> editorUsername);

	/**
	 * for each username, removes all permissions, deletes or invalidates all sessions, and sets the user to
	 * disabled. should be fault tolerant for usernames that do not exist.
	 * @param usernames to deprovision
	 * @param editorUsername (if manually triggered)
	 * @return returns the successfully deprovisioned usernames
	 */
	List<String> deprovisionUsers(List<String> usernames, Optional<String> editorUsername);

	/**
	 * restores previously deprovisioned users. should be fault tolerant for usernames that do not exist or are not
	 * in a restorable state.
	 * @param usernames to restore, which were previously deprovisioned using
	 * {@link UserDeprovisioningStrategy#deprovisionUsers(List, Optional)}
	 * @param editorUsername (if manually triggered)
	 * @return returns the successfully restored usernames
	 */
	List<String> restoreUsers(List<String> usernames, Optional<String> editorUsername);

}