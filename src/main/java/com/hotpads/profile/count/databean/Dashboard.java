package com.hotpads.profile.count.databean;

import java.util.Date;
import java.util.List;

import javax.persistence.Id;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.profile.count.databean.key.DashboardKey;

@SuppressWarnings("serial")
public class Dashboard extends BaseDatabean<DashboardKey,Dashboard>{

	/************************ fields ********************************/
	
	@Id
	protected DashboardKey key;
	protected Date created;
	protected Long userId;
	protected Boolean publicAccess;
	protected String name;
	protected boolean defaultDashboard;
	
	public static class F{
		public static final String
			created = "created",
			userId = "userId",
			publicAccess = "publicAccess",
			name = "name",
			defaultDashboard = "defaultDashboard";
	}
		
	public static class DashboardFielder extends BaseDatabeanFielder<DashboardKey,Dashboard>{
		public DashboardFielder(){}
		@Override
		public Class<DashboardKey> getKeyFielderClass(){
			return DashboardKey.class;
		}
		@Override
		public List<Field<?>> getNonKeyFields(Dashboard d){
			return FieldTool.createList(
					new DateField(F.created, d.created),
					new LongField(F.userId, d.userId),
					new BooleanField(F.publicAccess, d.publicAccess),
					new StringField(F.name, d.name, 255),
					new BooleanField(F.defaultDashboard, d.isDefaultDashboard()));
		}
	}
	
	/************************** constructor *****************************/
	
	Dashboard(){
		this.key = new DashboardKey();
	}
	
	public Dashboard(DashboardKey key, Long userId, Boolean publicAccess, String name){
		this.key = key;
		this.created = new Date(System.currentTimeMillis());
		this.userId = userId;
		this.publicAccess = publicAccess;
		this.name = name;
	}

	/*********************** Databean **********************************/

	@Override
	public Class<DashboardKey> getKeyClass() {
		return DashboardKey.class;
	}

	@Override
	public DashboardKey getKey() {
		return key;
	}

	/************************ ref ***************************************/

	public Dashboard getListingPaymentRecord() {
		return this;
	}

	/********************** get/set *************************************/

	public Date getCreated(){
		return created;
	}

	public void setCreated(Date created){
		this.created = created;
	}

	public Long getUserId(){
		return userId;
	}

	public void setUserId(Long userId){
		this.userId = userId;
	}

	public Boolean getPublicAccess(){
		return publicAccess;
	}

	public void setPublicAccess(Boolean publicAccess){
		this.publicAccess = publicAccess;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public void setKey(DashboardKey key){
		this.key = key;
	}

	/**
	 * @return the defaultDashboard
	 */
	public boolean isDefaultDashboard(){
		return defaultDashboard;
	}

	/**
	 * @param defaultDashboard the defaultDashboard to set
	 */
	public void setDefaultDashboard(boolean defaultDashboard){
		this.defaultDashboard = defaultDashboard;
	}
	
}