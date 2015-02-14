package com.hotpads.profile.count.databean;

import java.util.List;

import javax.persistence.Id;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseLatin1Fielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.profile.count.databean.key.DashboardEntryKey;

@SuppressWarnings("serial")
public class DashboardEntry extends BaseDatabean<DashboardEntryKey,DashboardEntry>{

	public static final int DEFAULT_STRING_LENGTH = MySqlColumnType.MAX_LENGTH_VARCHAR;
	
	/************************ fields ********************************/
	
	@Id
	protected DashboardEntryKey key;
	protected DashboardEntryType type;
	protected String entry;
	protected String entryArchive;
	protected Integer entryOrder;
	protected String serverName;
	protected String webAppName;
	protected String frequency;
	protected Integer periodMs;
	protected Integer rollPeriod;
	

	public static class F{
		public static final String
			type = "type",
			entry = "entry",
			entryArchive = "entryArchive",
			entryOrder = "entryOrder",
			serverName = "serverName",
			webAppName = "webAppName",
			frequency = "frequency",
			rollPeriod = "rollPeriod";
	}
	
	public static class DashboardEntryFielder extends BaseLatin1Fielder<DashboardEntryKey,DashboardEntry>{
		public DashboardEntryFielder(){}
		@Override
		public Class<DashboardEntryKey> getKeyFielderClass(){
			return DashboardEntryKey.class;
		}
		@Override
		public List<Field<?>> getNonKeyFields(DashboardEntry d ){
			return FieldTool.createList(
					new StringEnumField<DashboardEntryType>(DashboardEntryType.class, F.type, d.type, DEFAULT_STRING_LENGTH),
					new StringField(F.entry, d.entry, MySqlColumnType.MAX_LENGTH_MEDIUMTEXT),
					new StringField(F.entryArchive, d.entryArchive, DEFAULT_STRING_LENGTH),
					new IntegerField(F.entryOrder, d.entryOrder),
					new StringField(F.serverName, d.serverName, DEFAULT_STRING_LENGTH),
					new StringField(F.webAppName, d.webAppName, DEFAULT_STRING_LENGTH),
					new StringField(F.frequency, d.getFrequency(), DEFAULT_STRING_LENGTH),
					new IntegerField(F.rollPeriod, d.rollPeriod));
		}
	}
	
	/************************** constructor *****************************/
	
	DashboardEntry(){
		this.key = new DashboardEntryKey();
	}

	public DashboardEntry(DashboardEntryKey key, DashboardEntryType type, String entry, String entryArchive,
			String serverName, String webAppName, String frequency, Integer entryOrder){
		//TODO CHECK SERVER NAME AND WEB APP EXIST
		this.key = key;
		this.type = type;
		this.entry = entry;
		this.entryArchive = entryArchive;
		this.serverName = serverName.trim();
		this.webAppName = webAppName;
		this.entryOrder = entryOrder;
		this.setFrequency(frequency);
		this.periodMs = getPeriodFromEntryArchive(entryArchive);
		this.rollPeriod = 1;
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

	public String getServerName(){
		return serverName;
	}

	public void setServerName(String serverName){
		this.serverName = serverName;
	}

	public String getWebAppName(){
		return webAppName;
	}

	public void setWebApp(String webAppName){	
		this.webAppName = webAppName;
	}

	public int getPeriodMs(){
		return getPeriodFromEntryArchive(entryArchive);
	}

	public void setPeriodMs(int periodMs){
		this.setEntryArchive("databean "+periodMs);
		this.periodMs = periodMs;
	}

	//Useful method
	private int getPeriodFromEntryArchive(String entryArchive){
		String period = entryArchive.split(" ")[1];
		return new Integer(period);
	}

	public String getFrequency(){
		return frequency;
	}

	public void setFrequency(String frequency){
		this.frequency = frequency;
	}

	public Integer getRollPeriod(){
		if(rollPeriod==null){
			return 1;
		}
		return rollPeriod;
	}

	public void setRollPeriod(Integer rollPeriod){
		this.rollPeriod = rollPeriod;
	}
	
}