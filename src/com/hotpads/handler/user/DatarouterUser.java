package com.hotpads.handler.user;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.enums.DataRouterEnumTool;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.array.DelimitedStringArrayField;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanField;
import com.hotpads.datarouter.storage.key.unique.base.BaseStringUniqueKey;
import com.hotpads.handler.user.authenticate.DatarouterUserRole;
import com.hotpads.util.core.MapTool;

@SuppressWarnings("serial")
public class DatarouterUser extends BaseDatabean<DatarouterUserKey, DatarouterUser> {

	/********************* fields *************************/

	@Id
	private DatarouterUserKey key;

	private String email;
	private String userToken;
	private String passwordSalt;
	private String passwordDigest;
	private Boolean enabled;
	private List<String> roles;

	private Date created;
	private Date lastLoggedIn;


	public static class F {
		public static final String
			email = "email",
			userToken = "userToken",
			passwordSalt = "passwordSalt",
			passwordDigest = "passwordDigest",
			enabled = "enabled",
			roles = "roles",
			created = "created",
			lastLoggedIn = "lastLoggedIn";
	}

	@Override
	public List<Field<?>> getNonKeyFields(){
		return FieldTool.createList(
			new StringField(F.email, email, MySqlColumnType.MAX_LENGTH_VARCHAR),
			new StringField(F.userToken, userToken, MySqlColumnType.MAX_LENGTH_VARCHAR),
			new StringField(F.passwordSalt, passwordSalt, MySqlColumnType.MAX_LENGTH_VARCHAR),
			new StringField(F.passwordDigest, passwordDigest, MySqlColumnType.MAX_LENGTH_TEXT),
			new BooleanField(F.enabled, enabled),
			new DelimitedStringArrayField(F.roles, ",", roles),
			new DateField(F.created, created),
			new DateField(F.lastLoggedIn, lastLoggedIn));
	}

	/****************** fielder *****************************/

	public static class DatarouterUserFielder extends BaseDatabeanFielder<DatarouterUserKey,DatarouterUser>{
		@Override
		public Class<DatarouterUserKey> getKeyFielderClass() {
			return DatarouterUserKey.class;
		}
		@Override
		public List<Field<?>> getNonKeyFields(DatarouterUser reputationUser) {
			return reputationUser.getNonKeyFields();
		}
		@Override
		public Map<String, List<Field<?>>> getIndexes(DatarouterUser databean){
			Map<String,List<Field<?>>> indexesByName = MapTool.createTreeMap();
			indexesByName.put("index_email", new DatarouterUserByEmailLookup(null).getFields());
			indexesByName.put("index_userToken", new DatarouterUserByUserTokenLookup(null).getFields());
			return indexesByName;
		}
	}

	/******************  constructors **************************/

	public DatarouterUser(){
		this.key = new DatarouterUserKey();
	}

	public DatarouterUser(Long id){
		this.key = new DatarouterUserKey(id);
	}

	/******** databean ***********************************/

	@Override
	public Class<DatarouterUserKey> getKeyClass() {
		return DatarouterUserKey.class;
	}

	@Override
	public DatarouterUserKey getKey() {
		return this.key;
	}

	@Override
	public boolean isFieldAware() {
		return true;
	}


	/***************** indexes *****************/

	public static class DatarouterUserByEmailLookup extends BaseStringUniqueKey<DatarouterUserKey>{
		public DatarouterUserByEmailLookup(String email){
			super(email);
		}
		public List<Field<?>> getFields(){
			return FieldTool.createList(
				new StringField(F.email, id, MySqlColumnType.MAX_LENGTH_VARCHAR));
		}
	}

	public static class DatarouterUserByUserTokenLookup extends BaseStringUniqueKey<DatarouterUserKey>{
		public DatarouterUserByUserTokenLookup(String userToken){
			super(userToken);
		}
		public List<Field<?>> getFields(){
			return FieldTool.createList(
				new StringField(F.userToken, id, MySqlColumnType.MAX_LENGTH_VARCHAR));
		}
	}
	
	/***************** methods *****************/
	
	public List<DatarouterUserRole> getRoles(){
		return DataRouterEnumTool.fromPersistentStrings(DatarouterUserRole.anonymous, roles);
	}
	
	public void setRoles(Collection<DatarouterUserRole> roleEnums){
		roles = DataRouterEnumTool.getPersistentStrings(roleEnums);
		Collections.sort(roles);//for db readability, but don't rely on it
	}

	/***************** get/ set ***********/

	public void setKey(DatarouterUserKey key) {
		this.key = key;
	}

	public Date getCreated(){
		return this.created;
	}

	public void setCreated(Date created){
		this.created = created;
	}

	public String getEmail(){
		return this.email;
	}

	public void setEmail(String email){
		this.email = email;
	}

	public String getPasswordDigest(){
		return this.passwordDigest;
	}

	public void setPasswordDigest(String passwordDigest){
		this.passwordDigest = passwordDigest;
	}

	public Long getId(){
		return this.key.getId();
	}

	public void setId(Long id){
		this.key.setId(id);
	}

	public String getPasswordSalt(){
		return passwordSalt;
	}

	public void setPasswordSalt(String passwordSalt){
		this.passwordSalt = passwordSalt;
	}

	public String getUserToken(){
		return userToken;
	}

	public void setUserToken(String userToken){
		this.userToken = userToken;
	}

	public Boolean isEnabled(){
		return enabled;
	}

	public void setEnabled(Boolean enabled){
		this.enabled = enabled;
	}

	public Date getLastLoggedIn(){
		return lastLoggedIn;
	}

	public void setLastLoggedIn(Date lastLoggedIn){
		this.lastLoggedIn = lastLoggedIn;
	}

}

