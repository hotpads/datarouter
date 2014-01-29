package com.hotpads.profile.count.databean.key;

import java.util.List;
import java.util.Random;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.util.core.number.RandomTool;

@SuppressWarnings("serial")
public class DashboardKey extends BasePrimaryKey<DashboardKey>{

	/************************ fields *************************************/

	protected Long id; 

	public static class F{
		public static final String
		id = "id";
	}
	
	@Override
	public List<Field<?>> getFields() {
		return FieldTool.createList(
			new LongField(F.id, id));
	}

	/********************** constructors *********************************/
	
	public DashboardKey(){
		this.id = RandomTool.nextPositiveLong(new Random());
	}
	
	public DashboardKey(Long id){
		this.id = id;
	}

	/************************ ref ***************************************/
	
	public DashboardKey getDashboardKey() {
		return this;
	}

	/********************** get/set *************************************/

	public Long getId(){
		return id;
	}

	public void setId(long id){
		this.id = id;
	}

}