package com.hotpads.handler.user;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.DateFieldKey;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.field.imp.array.DelimitedStringArrayField;
import com.hotpads.datarouter.storage.field.imp.array.DelimitedStringArrayFieldKey;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanField;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanFieldKey;
import com.hotpads.datarouter.storage.key.unique.base.BaseStringUniqueKey;
import com.hotpads.handler.user.role.DatarouterUserRole;
import com.hotpads.util.core.enums.DatarouterEnumTool;

public class DatarouterUser extends BaseDatabean<DatarouterUserKey,DatarouterUser>{

	private DatarouterUserKey key;
	private String username;
	private String userToken;
	private String passwordSalt;
	private String passwordDigest;
	private Boolean enabled;
	private List<String> roles;
	private Date created;
	private Date lastLoggedIn;
	private Boolean apiEnabled;
	private String apiKey;
	private String secretKey;

	public static class FieldKeys{
		public static final StringFieldKey username = new StringFieldKey("username");
		public static final StringFieldKey userToken = new StringFieldKey("userToken");
		public static final StringFieldKey passwordSalt = new StringFieldKey("passwordSalt");
		public static final StringFieldKey passwordDigest = new StringFieldKey("passwordDigest")
				.withSize(MySqlColumnType.MAX_LENGTH_VARCHAR);
		public static final BooleanFieldKey enabled = new BooleanFieldKey("enabled");
		public static final DelimitedStringArrayFieldKey roles = new DelimitedStringArrayFieldKey("roles");
		public static final DateFieldKey created = new DateFieldKey("created");
		public static final DateFieldKey lastLoggedIn = new DateFieldKey("lastLoggedIn");
		public static final BooleanFieldKey apiEnabled = new BooleanFieldKey("apiEnabled");
		public static final StringFieldKey apiKey = new StringFieldKey("apiKey");
		public static final StringFieldKey secretKey = new StringFieldKey("secretKey");
	}

	public static class DatarouterUserFielder extends BaseDatabeanFielder<DatarouterUserKey,DatarouterUser>{

		public DatarouterUserFielder(){
			super(DatarouterUserKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(DatarouterUser user){
			return Arrays.asList(
					new StringField(FieldKeys.username, user.username),
					new StringField(FieldKeys.userToken, user.userToken),
					new StringField(FieldKeys.passwordSalt, user.passwordSalt),
					new StringField(FieldKeys.passwordDigest, user.passwordDigest),
					new BooleanField(FieldKeys.enabled, user.enabled),
					new DelimitedStringArrayField(FieldKeys.roles, user.roles),
					new DateField(FieldKeys.created, user.created),
					new DateField(FieldKeys.lastLoggedIn, user.lastLoggedIn),
					new BooleanField(FieldKeys.apiEnabled, user.apiEnabled),
					new StringField(FieldKeys.apiKey, user.apiKey),
					new StringField(FieldKeys.secretKey, user.secretKey));
		}
		@Override
		public Map<String, List<Field<?>>> getIndexes(DatarouterUser databean){
			Map<String,List<Field<?>>> indexesByName = new TreeMap<>();
			indexesByName.put("index_username", new DatarouterUserByUsernameLookup(null).getFields());
			indexesByName.put("index_userToken", new DatarouterUserByUserTokenLookup(null).getFields());
			indexesByName.put("index_apiKey", new DatarouterUserByApiKeyLookup(null).getFields());
			indexesByName.put("index_secretKey", new DatarouterUserBySecretKeyLookup(null).getFields());
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

	public static DatarouterUser create(Long id, String userToken, String email, String passwordSalt,
			String passwordDigest, Collection<DatarouterUserRole> roles, String apiKey, String secretKey){
		DatarouterUser user = new DatarouterUser();
		user.setId(id);
		Date now = new Date();
		user.setCreated(now);
		user.setLastLoggedIn(null);
		user.setEnabled(true);

		user.setUserToken(userToken);
		user.setUsername(email);
		user.setPasswordSalt(passwordSalt);
		user.setPasswordDigest(passwordDigest);
		user.setRoles(roles);

		user.setApiEnabled(true);
		user.setApiKey(apiKey);
		user.setSecretKey(secretKey);
		return user;
	}

	@Override
	public Class<DatarouterUserKey> getKeyClass(){
		return DatarouterUserKey.class;
	}

	@Override
	public DatarouterUserKey getKey(){
		return this.key;
	}

	public static class DatarouterUserByUsernameLookup extends BaseStringUniqueKey<DatarouterUserKey>{
		public DatarouterUserByUsernameLookup(String username){
			super(username);
		}

		@Override
		public List<Field<?>> getFields(){
			return Arrays.asList(
				new StringField(FieldKeys.username, id));
		}
	}

	public static class DatarouterUserByApiKeyLookup extends BaseStringUniqueKey<DatarouterUserKey>{
		public DatarouterUserByApiKeyLookup(String apiKey){
			super(apiKey);
		}
		@Override
		public List<Field<?>> getFields(){
			return Arrays.asList(
				new StringField(FieldKeys.apiKey, id));
		}
	}

	public static class DatarouterUserByUserTokenLookup extends BaseStringUniqueKey<DatarouterUserKey>{
		public DatarouterUserByUserTokenLookup(String userToken){
			super(userToken);
		}
		@Override
		public List<Field<?>> getFields(){
			return Arrays.asList(
				new StringField(FieldKeys.userToken, id));
		}
	}

	public static class DatarouterUserBySecretKeyLookup extends BaseStringUniqueKey<DatarouterUserKey>{
		public DatarouterUserBySecretKeyLookup(String secretKey){
			super(secretKey);
		}
		@Override
		public List<Field<?>> getFields(){
			return Arrays.asList(
				new StringField(FieldKeys.secretKey, id));
		}
	}

	public List<DatarouterUserRole> getRoles(){
		return DatarouterEnumTool.fromPersistentStrings(DatarouterUserRole.user, roles);
	}

	public void setRoles(Collection<DatarouterUserRole> roleEnums){
		roles = DatarouterEnumTool.getPersistentStrings(roleEnums);
		Collections.sort(roles);
	}

	public Date getCreated(){
		return this.created;
	}

	public void setCreated(Date created){
		this.created = created;
	}

	public String getUsername(){
		return this.username;
	}

	public void setUsername(String username){
		this.username = username;
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

	public Boolean getEnabled(){
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

	public Boolean getApiEnabled(){
		return apiEnabled;
	}

	public void setApiEnabled(Boolean apiEnabled){
		this.apiEnabled = apiEnabled;
	}

	public String getApiKey(){
		return apiKey;
	}

	public void setApiKey(String apiKey){
		this.apiKey = apiKey;
	}

	public String getSecretKey(){
		return secretKey;
	}

	public void setSecretKey(String secretKey){
		this.secretKey = secretKey;
	}

}

