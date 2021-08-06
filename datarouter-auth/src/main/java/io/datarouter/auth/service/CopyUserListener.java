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
package io.datarouter.auth.service;

/**
 * Allows specifying additional logic for copying users
 */
public interface CopyUserListener{

	/**
	 * this gets called when the admin UI is used to copy a user, after the copy has been completed successfully.
	 * @param oldUsername the old DatarouterUser username
	 * @param newUsername the new DatarouterUser username
	 */
	public void onCopiedUser(String oldUsername, String newUsername);

	public class DefaultCopyUserListener implements CopyUserListener{

		@Override
		public void onCopiedUser(String oldUsername, String newUsername){
		}

	}

}
