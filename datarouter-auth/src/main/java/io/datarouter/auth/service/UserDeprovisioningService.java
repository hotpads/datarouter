/**
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
package io.datarouter.auth.service;

import java.util.List;
import java.util.function.Supplier;

import javax.inject.Singleton;

public interface UserDeprovisioningService{

	/**
	 * flag users for later manual deprovisioning. should be fault tolerant for usernames that do not exist.
	 * @param usernames to flag
	 * @return returns the successfully flagged usernames
	 */
	List<String> flagUsersForDeprovisioning(List<String> usernames);

	/**
	 * for each username, removes all permissions, deletes or invalidates all sessions, and sets the user to disabled.
	 * should be fault tolerant for usernames that do not exist.
	 * @param usernames to deprovision
	 * @return returns the successfully deprovisioned usernames
	 */
	List<String> deprovisionUsers(List<String> usernames);

	/**
	 * calls {@link UserDeprovisioningService#flagUsersForDeprovisioning(List)} or
	 * {@link UserDeprovisioningService#deprovisionUsers(List)} depending on the result of
	 * {@link UserDeprovisioningService#shouldFlagUsersInsteadOfDeprovisioning()}
	 * @param usernames to flag or deprovision
	 * @return successfully flagged or deprovisioned usernames
	 */
	default List<String> flagOrDeprovisionUsers(List<String> usernames){
		if(shouldFlagUsersInsteadOfDeprovisioning()){
			return flagUsersForDeprovisioning(usernames);
		}
		return deprovisionUsers(usernames);
	}

	/**
	 * restores previously deprovisioned users. should be fault tolerant for usernames that do not exist or are not in a
	 * restorable state.
	 * @param usernames to restore, which were previously deprovisioned using
	 * {@link UserDeprovisioningService#deprovisionUsers(List)}
	 * @return returns the successfully restored usernames
	 */
	List<String> restoreDeprovisionedUsers(List<String> usernames);

	/**
	 * indicates whether {@link UserDeprovisioningService#deprovisionUsers(List)} users should be flagged for
	 * deprovisioning instead of being immediately deprovisioned
	 * @return true if user should not be immediately deprovisioned
	 */
	boolean shouldFlagUsersInsteadOfDeprovisioning();

	/**
	 * a class that can be used as a helper for implementations
	 */
	public static class ShouldFlagUsersInsteadOfDeprovisioningSupplier implements Supplier<Boolean>{

		private final boolean shouldFlagUsersInsteadOfDeprovisioning;

		public ShouldFlagUsersInsteadOfDeprovisioningSupplier(boolean shouldFlagUsersInsteadOfDeprovisioning){
			this.shouldFlagUsersInsteadOfDeprovisioning = shouldFlagUsersInsteadOfDeprovisioning;
		}

		@Override
		public Boolean get(){
			return shouldFlagUsersInsteadOfDeprovisioning;
		}

	}

	@Singleton
	public static class NoOpUserDeprovisioningService implements UserDeprovisioningService{

		@Override
		public List<String> flagUsersForDeprovisioning(List<String> usernames){
			return List.of();
		}

		@Override
		public List<String> deprovisionUsers(List<String> usernames){
			return List.of();
		}

		@Override
		public List<String> restoreDeprovisionedUsers(List<String> usernames){
			return List.of();
		}

		@Override
		public boolean shouldFlagUsersInsteadOfDeprovisioning(){
			return false;
		}

	}

}
