package com.hotpads.handler.user.session;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.enums.DataRouterEnumTool;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.array.DelimitedStringArrayField;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUserKey;
import com.hotpads.handler.user.authenticate.DatarouterTokenGenerator;
import com.hotpads.handler.user.role.DatarouterUserRole;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

/*
 * A single user may have multiple sessions via different computers, browsers, tabs, etc.  Create one of these for each
 * such session
 */
@SuppressWarnings("serial")
public class DatarouterSession 
extends BaseDatarouterSessionDatabean<DatarouterSessionKey, DatarouterSession> 
implements Serializable {	
//	private static Logger logger = Logger.getLogger(DatarouterSession.class);

	/****************** fields **************************/
	
	private Long userId;//needed to map back to the DatarouterUser
	
	//cached fields from DatarouterUser
	private String userToken;
	private String username;
	private Date userCreated;
	private List<String> roles;//TODO convert to List<DatarouterUserRole>
	
	public class F {
		public static final String
				KEY_NAME = "key",
				userId = "userId",
				userToken = "userToken",
				username = "username",
				userCreated = "userCreated",
				roles = "roles";
	}
	
	@Override
	public List<Field<?>> getNonKeyFields(){
		List<Field<?>> nonKeyFields = ListTool.createArrayList();
//		fields.add(new DateField(BaseDatarouterSessionDatabean.F.created, getCreated()));
		nonKeyFields.add(new DateField(BaseDatarouterSessionDatabean.F.updated, getUpdated()));
		
//		List<Field<?>> nonKeyFields = super.getNonKeyFields();
		nonKeyFields.add(new UInt63Field(F.userId, userId));
		nonKeyFields.add(new StringField(F.userToken, userToken, MySqlColumnType.MAX_LENGTH_VARCHAR));
		nonKeyFields.add(new StringField(F.username, username, MySqlColumnType.MAX_LENGTH_VARCHAR));
		nonKeyFields.add(new DelimitedStringArrayField(F.roles, ",", roles));
		nonKeyFields.add(new DateField(F.userCreated, userCreated));
		return nonKeyFields;
	}
	
	public static class DatarouterSessionFielder extends BaseDatabeanFielder<DatarouterSessionKey,DatarouterSession>{
		public DatarouterSessionFielder(){}
		@Override
		public Class<DatarouterSessionKey> getKeyFielderClass(){
			return DatarouterSessionKey.class;
		}
		@Override
		public List<Field<?>> getNonKeyFields(DatarouterSession d){
			return d.getNonKeyFields();
		}
	}
	
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
		return session;
	}
	
	public static DatarouterSession createFromUser(DatarouterUser user){
		DatarouterSession session = createAnonymousSession(user.getUserToken());
		session.setUserId(user.getId());
		session.setUserCreated(user.getCreated());
		session.setUsername(user.getUsername());
		session.setRoles(user.getRoles());//remember to overwrite the anonymous role
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
	/*
	 * careful, these are stored as strings right now, so watch for unchecked methods
	 */
	public List<DatarouterUserRole> getRoles(){
		return DataRouterEnumTool.fromPersistentStrings(DatarouterUserRole.user, roles);
	}
	
	public void setRoles(Collection<DatarouterUserRole> roleEnums){
		roles = DataRouterEnumTool.getPersistentStrings(roleEnums);
		Collections.sort(roles);//for db readability, but don't rely on it
	}
	
	public boolean doesUserHaveRole(DatarouterUserRole requiredRole) {
		return CollectionTool.nullSafe(roles).contains(requiredRole.getPersistentString());
	}
	
	public boolean isAnonymous(){
		return CollectionTool.isEmpty(roles);
	}
	
	public boolean isDatarouterAdmin(){
		return CollectionTool.nullSafe(roles).contains(DatarouterUserRole.datarouterAdmin.getPersistentString());
	}
	
	
	/*********************** get/set ************************************/
	
	public DatarouterSessionKey getKey() {
		return key;
	}
	
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
	
}
