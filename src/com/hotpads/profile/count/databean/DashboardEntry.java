package com.hotpads.profile.count.databean;

import java.util.List;

import javax.persistence.Id;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.profile.count.databean.key.DashboardEntryKey;

@SuppressWarnings("serial")
public class DashboardEntry extends BaseDatabean<DashboardEntryKey,DashboardEntry>{

	/************************ fields ********************************/
	
	@Id
	protected DashboardEntryKey key;
	protected DashboardEntryType type;
	protected String entry;
	protected String entryArchive;
	protected Integer entryOrder;
	
	public static class F{
		public static final String
			type = "type",
			entry = "entry",
			entryArchive = "entryArchive",
			entryOrder = "entryOrder";
	}
	
	@Override
	public List<Field<?>> getNonKeyFields(){
		return FieldTool.createList(
				new StringEnumField<DashboardEntryType>(DashboardEntryType.class, F.type, type, 255),
				new StringField(F.entry, entry, 255),
				new StringField(F.entryArchive, entryArchive, 255),
				new IntegerField(F.entryOrder, entryOrder));
	}
	
	public static class DashboardEntryFielder extends BaseDatabeanFielder<DashboardEntryKey,DashboardEntry>{
		public DashboardEntryFielder(){}
		@Override
		public Class<DashboardEntryKey> getKeyFielderClass(){
			return DashboardEntryKey.class;
		}
		@Override
		public List<Field<?>> getNonKeyFields(DashboardEntry d){
			return d.getNonKeyFields();
		}
	}
	
	/************************** constructor *****************************/
	
	DashboardEntry(){
		this.key = new DashboardEntryKey();
	}

	public DashboardEntry(DashboardEntryKey key, DashboardEntryType type, String entry, String entryArchive, Integer entryOrder){
		this.key = key;
		this.type = type;
		this.entry = entry;
		this.entryArchive = entryArchive;
		this.entryOrder = entryOrder;
	}

	/*********************** Databean **********************************/

	@Override
	public Class<DashboardEntryKey> getKeyClass() {
		return DashboardEntryKey.class;
	}

	@Override
	public DashboardEntryKey getKey() {
		return key;
	}
	
	@Override
	public boolean isFieldAware(){
		return true;
	}

	/************************ ref ***************************************/

	public DashboardEntry getListingPaymentRecord() {
		return this;
	}

	/********************** get/set *************************************/

	public DashboardEntryType getType(){
		return type;
	}

	public void setType(DashboardEntryType type){
		this.type = type;
	}

	public String getEntry(){
		return entry;
	}
	
	public void setEntry(String entry){
		this.entry = entry;
	}
	
	public String getEntryArchive(){
		return entryArchive;
	}

	public void setEntryArchive(String entryArchive){
		this.entryArchive = entryArchive;
	}

	public Integer getEntryOrder(){
		return entryOrder;
	}

	public void setEntryOrder(Integer entryOrder){
		this.entryOrder = entryOrder;
	}

	public void setKey(DashboardEntryKey key){
		this.key = key;
	}

}