package com.hotpads.handler.user.session;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;

import org.hibernate.annotations.AccessType;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.handler.user.DatarouterUserKey;
import com.hotpads.handler.user.authenticate.DatarouterUserRole;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

@SuppressWarnings("serial")
@Entity()
@AccessType("field")
public class DatarouterSession 
extends BaseDatarouterSessionDatabean<DatarouterSessionKey, DatarouterSession> 
implements Serializable {	
//	private static Logger logger = Logger.getLogger(DatarouterSession.class);

	/****************** fields **************************/
	
	private Long userId;
	private String userToken;
	private String email;
	private Date userCreated;
	private List<String> roles = ListTool.create();
	
	public class F {
		public static final String
				KEY_NAME = "key",
				userId = "userId",
				userToken = "userToken",
				email = "email",
				userCreated = "userCreated",
				roles = "roles";
	}
	
	@Override
	public List<Field<?>> getNonKeyFields() {
		List<Field<?>> fields =  FieldTool.createList(
				new DateField(BaseDatarouterSessionDatabean.F.updated, updated),
				new StringField(F.userToken, userToken, MySqlColumnType.MAX_LENGTH_VARCHAR),
				new StringField(F.email, email, MySqlColumnType.MAX_LENGTH_VARCHAR),
				new LongField(F.userId, userId),
				new DelimitedStringArrayField(F.roles, roles),
				new DateField(F.userCreated, userCreated));
		return fields;
	}
	
	public static class UserSessionFielder extends BaseDatabeanFielder<DatarouterSessionKey,DatarouterSession>{
		public UserSessionFielder(){}
		@Override
		public Class<DatarouterSessionKey> getKeyFielderClass(){
			return DatarouterSessionKey.class;
		}
		@Override
		public List<Field<?>> getNonKeyFields(DatarouterSession d){
			return d.getNonKeyFields();
		}
	}
	
	@Override
	public boolean isFieldAware(){
		return true;
	}
	
	public Class<DatarouterSessionKey> getKeyClass() {
		return DatarouterSessionKey.class;
	}

	/************************** construct *************************/
	
	public DatarouterSession(){
		super(new DatarouterSessionKey());
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
	
	public boolean doesUserHaveRole(DatarouterUserRole requiredRole) {
		return CollectionTool.nullSafe(roles).contains(requiredRole);
	}
	
	
	/*********************** get/set ************************************/
	
	public DatarouterSessionKey getKey() {
		return key;
	}
	
	public void setKey(DatarouterSessionKey key) {
		this.key = key;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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
}
