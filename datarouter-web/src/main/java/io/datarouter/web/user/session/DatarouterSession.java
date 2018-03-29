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
package io.datarouter.web.user.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.DateField;
import io.datarouter.model.field.imp.DateFieldKey;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.array.DelimitedStringArrayField;
import io.datarouter.model.field.imp.array.DelimitedStringArrayFieldKey;
import io.datarouter.model.field.imp.positive.UInt63Field;
import io.datarouter.model.field.imp.positive.UInt63FieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.enums.DatarouterEnumTool;
import io.datarouter.web.handler.types.HttpRequestBuilder;
import io.datarouter.web.user.authenticate.DatarouterTokenGenerator;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.databean.DatarouterUserKey;
import io.datarouter.web.user.role.DatarouterUserRole;
import io.datarouter.web.user.session.service.Session;

/*
 * A single user may have multiple sessions via different computers, browsers, tabs, etc.  Create one of these for each
 * such session
 */
@SuppressWarnings("serial")
public class DatarouterSession
extends BaseDatarouterSessionDatabean<DatarouterSessionKey, DatarouterSession>
implements Serializable, Session{
//	private static Logger logger = LoggerFactory.getLogger(DatarouterSession.class);

	/****************** fields **************************/

	private Long userId;//needed to map back to the DatarouterUser

	//cached fields from DatarouterUser
	private String userToken;
	private String username;
	private Date userCreated;
	private List<String> roles;
	private Boolean persistent = true;

	public static class FieldKeys{
		public static final UInt63FieldKey userId = new UInt63FieldKey("userId");
		public static final StringFieldKey userToken = new StringFieldKey("userToken");
		public static final StringFieldKey username = new StringFieldKey("username");
		public static final DelimitedStringArrayFieldKey roles = new DelimitedStringArrayFieldKey("roles");
		public static final DateFieldKey userCreated = new DateFieldKey("userCreated");
	}

	public static class DatarouterSessionFielder extends BaseDatabeanFielder<DatarouterSessionKey,DatarouterSession>{
		public DatarouterSessionFielder(){
			super(DatarouterSessionKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(DatarouterSession databean){
			List<Field<?>> nonKeyFields = new ArrayList<>(databean.getNonKeyFields());
			nonKeyFields.add(new UInt63Field(FieldKeys.userId, databean.userId));
			nonKeyFields.add(new StringField(FieldKeys.userToken, databean.userToken));
			nonKeyFields.add(new StringField(FieldKeys.username, databean.username));
			nonKeyFields.add(new DelimitedStringArrayField(FieldKeys.roles, databean.roles));
			nonKeyFields.add(new DateField(FieldKeys.userCreated, databean.userCreated));
			return nonKeyFields;
		}

	}

	@Override
	public Class<DatarouterSessionKey> getKeyClass(){
		return DatarouterSessionKey.class;
	}

	/************************** construct *************************/

	public DatarouterSession(){
		super(new DatarouterSessionKey(null));
	}

	public static DatarouterSession createAnonymousSession(String userToken){
		DatarouterSession session = new DatarouterSession();
		session.setUserToken(userToken);
		session.setSessionToken(DatarouterTokenGenerator.generateRandomToken());
		Date now = new Date();
		session.setCreated(now);
		session.setUpdated(now);
		session.setRoles(null);
		return session;
	}

	public static DatarouterSession createFromUser(DatarouterUser user){
		DatarouterSession session = createAnonymousSession(user.getUserToken());
		session.setUserId(user.getId());
		session.setUserCreated(user.getCreated());
		session.setUsername(user.getUsername());
		session.setRoles(user.getRoles());
		return session;
	}


	/*********************** static methods ************************************/

	public static DatarouterSession nullSafe(DatarouterSession in){
		return in == null ? new DatarouterSession() : in;
	}


	/********************** methods *************************************/

	public DatarouterUserKey getUserKey(){
		if(userId == null){
			return null;
		}
		return new DatarouterUserKey(userId);
	}


	/************** DatarouterUserRole methods *****************************/

	public Collection<DatarouterUserRole> getRoles(){
		return DatarouterEnumTool.fromPersistentStrings(DatarouterUserRole.user, roles);
	}

	public void setRoles(Collection<DatarouterUserRole> roleEnums){
		roles = DatarouterEnumTool.getPersistentStrings(roleEnums);
		Collections.sort(roles);
	}

	public boolean doesUserHaveRole(DatarouterUserRole requiredRole){
		return getRoles().contains(requiredRole);
	}

	public boolean isAnonymous(){
		return CollectionTool.isEmpty(roles);
	}

	public boolean isRequestorOnly(){
		return getRoles().size() == 1 && doesUserHaveRole(DatarouterUserRole.requestor);
	}

	public boolean isDatarouterAdmin(){
		return doesUserHaveRole(DatarouterUserRole.datarouterAdmin);
	}

	public boolean isAdmin(){
		return doesUserHaveRole(DatarouterUserRole.datarouterAdmin) || doesUserHaveRole(DatarouterUserRole.admin);
	}

	public boolean isApiUser(){
		return doesUserHaveRole(DatarouterUserRole.apiUser);
	}

	public boolean isDocUser(){
		return doesUserHaveRole(DatarouterUserRole.docUser);
	}

	/*********************** get/set ************************************/

	@Override
	public DatarouterSessionKey getKey(){
		return key;
	}

	@Override
	public void setKey(DatarouterSessionKey key){
		this.key = key;
	}

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

	public Long getUserId(){
		return userId;
	}

	public void setUserId(Long userId){
		this.userId = userId;
	}

	public Date getUserCreated(){
		return userCreated;
	}

	public void setUserCreated(Date userCreated){
		this.userCreated = userCreated;
	}

	public Boolean getPersistent(){
		return persistent;
	}

	public void setPersistent(Boolean persistent){
		this.persistent = persistent;
	}

	public static final class DatarouterSessionMock{

		public static HttpServletRequest getAnonymousHttpServletRequest(){
			return new HttpRequestBuilder()
					.withAttribute(DatarouterSessionManager.REQUEST_ATTRIBUTE_NAME, buildSessionWithRoles(
							(DatarouterUserRole[])null))
					.build();
		}

		public static HttpServletRequest getAllRolesHttpServletRequest(){
			return new HttpRequestBuilder()
					.withAttribute(DatarouterSessionManager.REQUEST_ATTRIBUTE_NAME, buildSessionWithRoles(
							DatarouterUserRole.values()))
					.build();
		}

		public static HttpServletRequest getHttpServletRequestWithSessionRoles(DatarouterUserRole... roles){
			return new HttpRequestBuilder()
					.withAttribute(DatarouterSessionManager.REQUEST_ATTRIBUTE_NAME, buildSessionWithRoles(roles))
					.build();
		}

		public static DatarouterSession buildSessionWithRoles(DatarouterUserRole... roles){
			DatarouterSession session = new DatarouterSession();
			if(roles != null && roles.length > 0){
				session.setRoles(Arrays.asList(roles));
			}
			return session;
		}
	}
}
