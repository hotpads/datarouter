package com.hotpads.handler.user.session;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseLatin1Fielder;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.enums.DatarouterEnumTool;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.array.DelimitedStringArrayField;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUserKey;
import com.hotpads.handler.user.authenticate.DatarouterTokenGenerator;
import com.hotpads.handler.user.role.DatarouterUserRole;

/*
 * A single user may have multiple sessions via different computers, browsers, tabs, etc.  Create one of these for each
 * such session
 */
@SuppressWarnings("serial")
public class DatarouterSession 
extends BaseDatarouterSessionDatabean<DatarouterSessionKey, DatarouterSession> 
implements Serializable {	
//	private static Logger logger = LoggerFactory.getLogger(DatarouterSession.class);

	/****************** fields **************************/
	
	private Long userId;//needed to map back to the DatarouterUser
	
	//cached fields from DatarouterUser
	private String userToken;
	private String username;
	private Date userCreated;
	private List<String> roles;
	private boolean persistent = true;
	
	public class F {
		public static final String
				KEY_NAME = "key",
				userId = "userId",
				userToken = "userToken",
				username = "username",
				userCreated = "userCreated",
				roles = "roles";
	}
	
	
	public static class DatarouterSessionFielder extends BaseLatin1Fielder<DatarouterSessionKey,DatarouterSession>{
		public DatarouterSessionFielder(){}
		@Override
		public Class<DatarouterSessionKey> getKeyFielderClass(){
			return DatarouterSessionKey.class;
		}
		@Override
		public List<Field<?>> getNonKeyFields(DatarouterSession d){
			List<Field<?>> nonKeyFields = DrListTool.createArrayList();
//			fields.add(new DateField(BaseDatarouterSessionDatabean.F.created, getCreated()));
			nonKeyFields.add(new DateField(BaseDatarouterSessionDatabean.F.updated, d.getUpdated()));
			
//			List<Field<?>> nonKeyFields = super.getNonKeyFields();
			nonKeyFields.add(new UInt63Field(F.userId, d.userId));
			nonKeyFields.add(new StringField(F.userToken, d.userToken, MySqlColumnType.MAX_LENGTH_VARCHAR));
			nonKeyFields.add(new StringField(F.username, d.username, MySqlColumnType.MAX_LENGTH_VARCHAR));
			nonKeyFields.add(new DelimitedStringArrayField(F.roles, ",", d.roles));
			nonKeyFields.add(new DateField(F.userCreated, d.userCreated));
			return nonKeyFields;
		}
		
	}
	
	@Override
	public Class<DatarouterSessionKey> getKeyClass() {
		return DatarouterSessionKey.class;
	}

	/************************** construct *************************/
	
	DatarouterSession(){
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
		return in==null ? new DatarouterSession() : in;
	}

	
	/********************** methods *************************************/
		
	public DatarouterUserKey getUserKey(){
		if(userId==null){ return null; }
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
	
	public boolean doesUserHaveRole(DatarouterUserRole requiredRole) {
		return getRoles().contains(requiredRole);
	}
	
	public boolean isAnonymous(){
		return DrCollectionTool.isEmpty(roles);
	}
	
	public boolean isDatarouterAdmin(){
		return getRoles().contains(DatarouterUserRole.datarouterAdmin);
	}
	
	public boolean isAdmin() {
		Collection<DatarouterUserRole> rolesNullSafe = getRoles();
		return rolesNullSafe.contains(DatarouterUserRole.datarouterAdmin) ||
				rolesNullSafe.contains(DatarouterUserRole.admin);
	}
	
	public boolean isApiUser() {
		return getRoles().contains(DatarouterUserRole.apiUser);
	}
	
	
	/*********************** get/set ************************************/
	
	@Override
	public DatarouterSessionKey getKey() {
		return key;
	}
	
	@Override
	public void setKey(DatarouterSessionKey key) {
		this.key = key;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public Long getId() {
		return userId;
	}
	
	public void setId(Long id) {
		this.userId = id;
	}
	
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

	public boolean isPersistent() {
		return persistent;
	}
	
	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}
}
