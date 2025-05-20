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

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Executes the {@link UserDeprovisioningStrategy} and {@link UserDeprovisioningListeners} configured in
 * DatarouterAuthPlugin
 */
@Singleton
public class UserDeprovisioningService{

	@Inject
	private UserDeprovisioningStrategy userDeprovisioningServiceStrategy;
	@Inject
	private UserDeprovisioningListeners listeners;

	/**
	 * executes {@link UserDeprovisioningStrategy#deprovisionUsers(List)}, proceeded by
	 * {@link UserDeprovisioningListener#onDeprovisionedUsers(List)} from {@link UserDeprovisioningListeners}
	 * @param usernames to deprovision
	 */
	public final void deprovisionUsers(List<String> usernames){
		userDeprovisioningServiceStrategy.deprovisionUsers(usernames);
		listeners.get().forEach(listener -> listener.onDeprovisionedUsers(usernames));
	}

}
