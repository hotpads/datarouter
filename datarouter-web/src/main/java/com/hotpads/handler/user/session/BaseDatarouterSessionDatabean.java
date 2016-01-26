package com.hotpads.handler.user.session;

import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import com.hotpads.datarouter.storage.databean.BaseDatabean;

@MappedSuperclass 
@Access(AccessType.FIELD)
public abstract class BaseDatarouterSessionDatabean<
		PK extends BaseDatarouterSessionDatabeanKey<PK>,
		D extends BaseDatarouterSessionDatabean<PK,D>> 
extends BaseDatabean<PK,D>{
	
	@Id
	protected PK key;
	private Date created;//track how old the session is
	private Date updated;//last heartbeat time

	
	public static class F{
		public static final String 
			created = "created",
			updated = "updated";
	}
	
//	public List<Field<?>> getNonKeyFields() {
//		return FieldTool.createList(
//			new DateField(F.created, created),
//			new DateField(F.updated, updated));//this should probably be LongDateField for mysql
//	}
	
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
