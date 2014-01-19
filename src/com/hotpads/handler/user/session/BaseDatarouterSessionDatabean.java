package com.hotpads.handler.user.session;

import java.util.Date;
import java.util.List;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.AccessType;

import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.DateField;

@SuppressWarnings("serial")
@MappedSuperclass 
@AccessType("field")
public abstract class BaseDatarouterSessionDatabean<
		PK extends BaseDatarouterSessionDatabeanKey<PK>,
		D extends BaseDatarouterSessionDatabean<PK,D>> 
extends BaseDatabean<PK,D>{
	
	@Id
	protected PK key;
	private Date created;//track how old the session is
	private Date updated;

	
	public static class F{
		public static final String 
			created = "created",
			updated = "updated";
	}
	
	@Override
	public List<Field<?>> getNonKeyFields() {
		return FieldTool.createList(
			new DateField(F.created, created),
			new DateField(F.updated, updated));
	}
	
	protected BaseDatarouterSessionDatabean(PK key){
		this.updated = new Date();
		this.key = key;
	}

	public PK getKey(){
		return key;
	}

	public void setKey(PK key){
		this.key = key;
	}
	
	public String getSessionToken(){
		return key.getSessionToken();
	}

	public void setSessionToken(String token){
		this.key.setSessionToken(token);
	}

	public Date getUpdated(){
		return updated;
	}

	public void setUpdated(Date updated){
		this.updated = updated;
	}

	public Date getCreated(){
		return created;
	}

	public void setCreated(Date created){
		this.created = created;
	}
	
}
