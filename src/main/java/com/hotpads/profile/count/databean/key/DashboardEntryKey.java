package com.hotpads.profile.count.databean.key;

import java.util.List;
import java.util.Random;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.util.core.number.RandomTool;

@SuppressWarnings("serial")
public class DashboardEntryKey extends BasePrimaryKey<DashboardEntryKey>{

	/************************ fields *************************************/

	protected Long id;
	protected Long dashboardId;

	public static class F{
		public static final String
			id = "id",
			dashboardId = "dashboardId";
	}
	
	@Override
	public List<Field<?>> getFields() {
		return FieldTool.createList(
			new LongField(F.dashboardId,dashboardId),
			new LongField(F.id, id));
	}

	/********************** constructors *********************************/
	
	public DashboardEntryKey(){
		this.id = RandomTool.nextPositiveLong(new Random());
	}
	
	public DashboardEntryKey(DashboardKey dashboardKey){
		this();
		this.dashboardId = dashboardKey.getId();
	}
	
	public DashboardEntryKey(Long id, DashboardKey dashboardKey){
		this.id = id;
		this.dashboardId = dashboardKey.getId();
	}

	/************************ ref ***************************************/
	
	public DashboardEntryKey getDashboardEntryKey() {
		return this;
	}

	/********************** get/set *************************************/

	public long getId(){
		return id;
	}

	public void setId(Long id){
		this.id = id;
	}
	
	public Long getDashboardId(){
		return dashboardId;
	}

	public void setDashboardId(DashboardKey dashboardId){
		this.dashboardId = dashboardId.getId();
	}

}