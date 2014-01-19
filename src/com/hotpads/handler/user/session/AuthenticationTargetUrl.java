package com.hotpads.handler.user.session;

import java.util.List;

import javax.persistence.Entity;

import org.hibernate.annotations.AccessType;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;

@SuppressWarnings("serial")
@Entity()
@AccessType("field")
@Deprecated//use cookie
public class AuthenticationTargetUrl 
extends BaseDatarouterSessionDatabean<AuthenticationTargetUrlKey,AuthenticationTargetUrl>{

	private String value;
	
	public static class F{
		public static final String
			value = "value";
	}
	
	@Override
	public List<Field<?>> getNonKeyFields() {
		List<Field<?>> fields = super.getNonKeyFields();
		fields.add(new StringField(F.value, value, MySqlColumnType.MAX_LENGTH_MEDIUMTEXT));
		return fields;
	}
	
	public static class AuthenticationTargetUrlFielder 
	extends BaseDatabeanFielder<AuthenticationTargetUrlKey,AuthenticationTargetUrl>{
		public AuthenticationTargetUrlFielder(){}
		@Override
		public Class<AuthenticationTargetUrlKey> getKeyFielderClass(){
			return AuthenticationTargetUrlKey.class;
		}
		@Override
		public List<Field<?>> getNonKeyFields(AuthenticationTargetUrl d){
			return d.getNonKeyFields();
		}
	}
	
	
	/******************* construct *************************/

	public AuthenticationTargetUrl(){
		super(new AuthenticationTargetUrlKey());
	}
	
	public AuthenticationTargetUrl(String sessionToken){
		super(new AuthenticationTargetUrlKey(sessionToken));
	}
	
	public AuthenticationTargetUrl(String sessionToken, String value){
		this(sessionToken);
		this.value = value;
	}
	
	
	/*********************** databean ************************/
	
	@Override
	public Class<AuthenticationTargetUrlKey> getKeyClass(){
		return AuthenticationTargetUrlKey.class;
	}
	
	@Override
	public boolean isFieldAware(){
		return true;
	}
	
	
	/******************** get/set ***************************/

	public String getValue(){
		return value;
	}

	public void setValue(String value){
		this.value = value;
	}
}
