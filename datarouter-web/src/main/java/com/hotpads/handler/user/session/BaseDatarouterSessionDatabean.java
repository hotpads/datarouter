package com.hotpads.handler.user.session;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.DateField;

public abstract class BaseDatarouterSessionDatabean<
		PK extends BaseDatarouterSessionDatabeanKey<PK>,
		D extends BaseDatarouterSessionDatabean<PK,D>>
extends BaseDatabean<PK,D>{

	protected PK key;
	private Date created;//track how old the session is
	private Date updated;//last heartbeat time


	public static class F{
		public static final String
			created = "created",
			updated = "updated";
	}

	public List<Field<?>> getNonKeyFields(){
		return Arrays.asList(
			new DateField(F.created, created),
			new DateField(F.updated, updated));//this should probably be LongDateField for mysql
	}

	protected BaseDatarouterSessionDatabean(PK key){
		this.updated = new Date();
		this.key = key;
	}

	@Override
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
