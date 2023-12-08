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
package io.datarouter.auth.storage.user.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import io.datarouter.auth.authenticate.DatarouterTokenGenerator;
import io.datarouter.auth.role.Role;
import io.datarouter.auth.role.RoleEnum;
import io.datarouter.auth.session.Session;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUserKey;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.codec.MilliTimeFieldCodec;
import io.datarouter.model.field.codec.StringListToBinaryCsvFieldCodec;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.array.ByteArrayEncodedField;
import io.datarouter.model.field.imp.array.ByteArrayEncodedFieldKey;
import io.datarouter.model.field.imp.comparable.LongEncodedField;
import io.datarouter.model.field.imp.comparable.LongEncodedFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.scanner.IterableScanner;
import io.datarouter.scanner.Scanner;
import io.datarouter.types.MilliTime;

/*
 * A single user may have multiple sessions via different computers, browsers, tabs, etc.  Create one of these for each
 * such session
 */

public class DatarouterSession
extends BaseDatarouterSessionDatabean<DatarouterSessionKey,DatarouterSession>
implements Session{

	private Long userId;//needed to map back to the DatarouterUser

	//cached fields from DatarouterUser
	private String userToken;
	private String username;
	private MilliTime userCreatedAt;
	private List<String> roles;
	private Boolean persistent = true;

	public static class FieldKeys{
		public static final LongFieldKey userId = new LongFieldKey("userId");
		public static final StringFieldKey userToken = new StringFieldKey("userToken");
		public static final StringFieldKey username = new StringFieldKey("username");
		public static final ByteArrayEncodedFieldKey<List<String>> roles
				= new ByteArrayEncodedFieldKey<>("roles", StringListToBinaryCsvFieldCodec.INSTANCE)
				.withSize(CommonFieldSizes.MAX_LENGTH_LONGBLOB);
		public static final LongEncodedFieldKey<MilliTime> userCreatedAt = new LongEncodedFieldKey<>(
				"userCreatedAt",
				new MilliTimeFieldCodec());
	}

	public static class DatarouterSessionFielder extends BaseDatabeanFielder<DatarouterSessionKey,DatarouterSession>{

		public DatarouterSessionFielder(){
			super(DatarouterSessionKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(DatarouterSession databean){
			List<Field<?>> nonKeyFields = new ArrayList<>(databean.getNonKeyFields());
			nonKeyFields.add(new LongField(FieldKeys.userId, databean.userId));
			nonKeyFields.add(new StringField(FieldKeys.userToken, databean.userToken));
			nonKeyFields.add(new StringField(FieldKeys.username, databean.username));
			nonKeyFields.add(new ByteArrayEncodedField<>(FieldKeys.roles, databean.roles));
			nonKeyFields.add(new LongEncodedField<>(FieldKeys.userCreatedAt, databean.userCreatedAt));
			return nonKeyFields;
		}

	}

	@Override
	public Supplier<DatarouterSessionKey> getKeySupplier(){
		return DatarouterSessionKey::new;
	}

	public DatarouterSession(){
		super(new DatarouterSessionKey(null));
	}

	public static DatarouterSession createAnonymousSession(String userToken){
		var session = new DatarouterSession();
		session.setUserToken(userToken);
		session.getKey().setSessionToken(DatarouterTokenGenerator.generateRandomToken());
		MilliTime now = MilliTime.now();
		session.setCreated(now.toDate());
		session.setUpdated(now.toDate());
		session.setRoles(Collections.emptyList());
		return session;
	}

	public static DatarouterSession createFromUser(DatarouterUser user){
		DatarouterSession session = createAnonymousSession(user.getUserToken());
		session.setUserId(user.getId());
		session.setUserCreated(user.getCreated());
		session.setUsername(user.getUsername());
		session.setRoles(user.getRolesIgnoreSaml());
		return session;
	}

	public static DatarouterSession nullSafe(DatarouterSession in){
		return in == null ? new DatarouterSession() : in;
	}

	public static boolean equals(DatarouterSession first, DatarouterSession second){
		return first.equals(second)
				&& Objects.equals(first.getUserId(), second.getUserId())
				&& Objects.equals(first.getUserToken(), second.getUserToken())
				&& Objects.equals(first.getUsername(), second.getUsername())
				&& Objects.equals(first.getUserCreated(), second.getUserCreated())
				&& Objects.equals(first.getRoles(), second.getRoles())
				&& Objects.equals(first.getPersistent(), second.getPersistent())
				&& Objects.equals(first.getCreated(), second.getCreated())
				&& Objects.equals(first.getUpdated(), second.getUpdated());
	}

	public DatarouterUserKey getUserKey(){
		if(userId == null){
			return null;
		}
		return new DatarouterUserKey(userId);
	}

	/*---------------------------- Role methods -----------------------------*/

	public Collection<Role> getRoles(){
		return IterableScanner.ofNullable(roles).map(Role::new).list();
	}

	public void setRoles(Collection<Role> roles){
		this.roles = Scanner.of(roles)
				.map(Role::getPersistentString)
				.sort()
				.distinct()
				.list();
	}

	public boolean isAnonymous(){
		return roles == null || roles.isEmpty();
	}

	public boolean hasRole(Role role){
		return roles.contains(role.getPersistentString());
	}

	public boolean hasRole(RoleEnum<?> role){
		return hasRole(role.getRole());
	}

	@Override
	public String getUsername(){
		return username;
	}

	public void setUsername(String username){
		this.username = username;
	}

	public Long getId(){
		return userId;
	}

	public void setId(Long id){
		this.userId = id;
	}

	@Override
	public String getUserToken(){
		return userToken;
	}

	public void setUserToken(String userToken){
		this.userToken = userToken;
	}

	@Override
	public Long getUserId(){
		return userId;
	}

	public void setUserId(Long userId){
		this.userId = userId;
	}

	public MilliTime getUserCreated(){
		return userCreatedAt;
	}

	public void setUserCreated(MilliTime userCreated){
		this.userCreatedAt = userCreated;
	}

	public Boolean getPersistent(){
		return persistent;
	}

	public void setPersistent(Boolean persistent){
		this.persistent = persistent;
	}

	@Override
	public String getSessionToken(){
		return getKey().getSessionToken();
	}

	/* TODO braydonh re-enable this
	public static final class DatarouterSessionMock{

		public static HttpServletRequest getAnonymousHttpServletRequest(){
			return new MockHttpServletRequestBuilder()
					.withAttribute(
							DatarouterSessionManager.DATAROUTER_SESSION_ATTRIBUTE,
							buildSessionWithRoles((DatarouterUserRole[])null))
					.build();
		}

		public static HttpServletRequest getAllDatarouterUserRolesHttpServletRequest(){
			return new MockHttpServletRequestBuilder()
					.withAttribute(
							DatarouterSessionManager.DATAROUTER_SESSION_ATTRIBUTE,
							buildSessionWithRoles(DatarouterUserRole.values()))
					.build();
		}

		public static HttpServletRequest getHttpServletRequestWithSessionRoles(RoleEnum<?>... roles){
			return new MockHttpServletRequestBuilder()
					.withAttribute(DatarouterSessionManager.DATAROUTER_SESSION_ATTRIBUTE, buildSessionWithRoles(roles))
					.build();
		}

		public static DatarouterSession buildSessionWithRoles(RoleEnum<?>... roles){
			DatarouterSession session = new DatarouterSession();
			if(roles != null && roles.length > 0){
				session.setRoles(Scanner.of(roles)
						.map(RoleEnum::getRole)
						.collect(HashSet::new));
			}
			return session;
		}
	}
	*/
}
