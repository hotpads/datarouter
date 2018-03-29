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
package io.datarouter.web.user;

import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.databean.DatarouterUser.DatarouterUserByUserTokenLookup;
import io.datarouter.web.user.databean.DatarouterUser.DatarouterUserByUsernameLookup;
import io.datarouter.web.user.databean.DatarouterUserKey;
import io.datarouter.web.user.role.DatarouterUserRole;
import io.datarouter.web.user.session.DatarouterSession;
import io.datarouter.web.util.PasswordTool;

@Singleton
public class DatarouterUserDao{
	private final IndexedSortedMapStorage<DatarouterUserKey, DatarouterUser> userNode;

	@Inject
	public DatarouterUserDao(DatarouterUserNodes nodes){
		userNode = nodes.getUserNode();
	}

	/** DatarouterUser **/

	public DatarouterUser getAndValidateCurrentUser(DatarouterSession session){
		DatarouterUser user = getUserBySession(session);
		if(user == null || !user.getEnabled()){
			throw new RuntimeException("Current user does not exist or is not enabled.");
		}
		return user;
	}

	public DatarouterUser getUserBySession(DatarouterSession session){
		if(session == null || session.getUserId() == null){
			return null;
		}
		return userNode.get(new DatarouterUserKey(session.getUserId()), null);
	}

	public DatarouterUser getUserById(Long id){
		return userNode.get(new DatarouterUserKey(id), null);
	}

	/** DatarouterUser permissions and business logic **/

	public static boolean canEditUser(DatarouterUser user, DatarouterUser editor){
		return user.equals(editor)
				|| !user.getRoles().contains(DatarouterUserRole.datarouterAdmin)
				&& DatarouterUserRole.isUserAdmin(editor)
				&& editor.getEnabled();
	}

	public static boolean canHavePassword(DatarouterUser user){
		return user.getPasswordDigest() != null || user.getRoles().contains(DatarouterUserRole.datarouterAdmin);
	}

	public static boolean isPasswordCorrect(DatarouterUser user, String rawPassword){
		if(user == null || rawPassword == null){
			return false;
		}
		String passwordDigest = PasswordTool.digest(user.getPasswordSalt(), rawPassword);
		return Objects.equals(user.getPasswordDigest(), passwordDigest);
	}

	public boolean isPasswordCorrect(String email, String rawPassword){
		DatarouterUser user = userNode.lookupUnique(new DatarouterUserByUsernameLookup(email), null);
		return isPasswordCorrect(user, rawPassword);
	}

	public static Set<DatarouterUserRole> getAllowedUserRoles(DatarouterUser currentUser, String[] userRoleStrings){
		Set<DatarouterUserRole> userRoles = DatarouterUserRole.fromStringArray(userRoleStrings);
		Set<DatarouterUserRole> validRoles = DatarouterUserRole.getPermissibleRolesForUser(currentUser);
		userRoles.retainAll(validRoles);
		userRoles.add(DatarouterUserRole.requestor);//everyone should have this
		return userRoles;
	}

	public void assertUserDoesNotExist(Long id, String userToken, String username){
		DatarouterUser userWithId = getUserById(id);
		if(userWithId != null){
			throw new IllegalArgumentException("DatarouterUser already exists with id=" + id);
		}
		DatarouterUser userWithUserToken = userNode.lookupUnique(
				new DatarouterUserByUserTokenLookup(userToken), null);
		if(userWithUserToken != null){
			throw new IllegalArgumentException("DatarouterUser already exists with userToken=" + userToken);
		}
		DatarouterUser userWithEmail = userNode.lookupUnique(
				new DatarouterUserByUsernameLookup(username), null);
		if(userWithEmail != null){
			throw new IllegalArgumentException("DatarouterUser already exists with username=" + username);
		}
	}
}
