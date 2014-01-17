package com.hotpads.handler.user;

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
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.unique.base.BaseStringUniqueKey;
import com.hotpads.util.core.MapTool;


/** CREATE SCRIPT
com.hotpads.reputation.databean.user.ReputationUser{
	PK{
		UInt63Field id
	}
	DateField created,
	StringField email,
	StringField password,
	index(email)
}

*/
@SuppressWarnings("serial")
@Entity
@AccessType("field")
public class DatarouterUser extends BaseDatabean<DatarouterUserKey, DatarouterUser> {

	/** fields ********************************************************************************************************/

	@Id
	private DatarouterUserKey key;

	private Date created;
	private String email;
	private String passwordSalt;
	private String passwordDigest;


	public static class F {
		public static final String
			created = "created",
			email = "email",
			passwordSalt = "passwordSalt",
			passwordDigest = "passwordDigest";
	}

	@Override
	public List<Field<?>> getNonKeyFields(){
		return FieldTool.createList(
			new DateField(F.created, created),
			new StringField(F.email, email, MySqlColumnType.MAX_LENGTH_VARCHAR),
			new StringField(F.passwordSalt, passwordSalt, MySqlColumnType.MAX_LENGTH_VARCHAR),
			new StringField(F.passwordDigest, passwordDigest, MySqlColumnType.MAX_LENGTH_TEXT));//using SHA256
	}

	/** fielder *******************************************************************************************************/

	public static class ReputationUserFielder
		extends BaseDatabeanFielder<DatarouterUserKey, DatarouterUser>{
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
			indexesByName.put("index_email", new ReputationUserByEmailLookup(null).getFields());
			return indexesByName;
		}
	}

	/** constructors **************************************************************************************************/

	public DatarouterUser(){
		this.key = new DatarouterUserKey();
	}

	public DatarouterUser(Long id){
		this.key = new DatarouterUserKey(id);
	}

	/** databean ******************************************************************************************************/

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


	/** indexes *******************************************************************************************************/

	public static class ReputationUserByEmailLookup extends BaseStringUniqueKey<DatarouterUserKey>{

		public ReputationUserByEmailLookup(String email){
			super(email);
		}

		public List<Field<?>> getFields(){
			return FieldTool.createList(
				new StringField(F.email, id, MySqlColumnType.MAX_LENGTH_VARCHAR));
		}
	}

	/** getters/setters ***********************************************************************************************/

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

}

