package com.hotpads.profile.count.databean.alert;

import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.serialize.fielder.Fielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.key.multi.BaseLookup;
import com.hotpads.profile.count.databean.key.CounterAlertDestinationKey;
import com.hotpads.profile.count.databean.key.CounterAlertDestinationKey.F;
import com.hotpads.util.core.MapTool;

@SuppressWarnings("serial")
@Entity
@AccessType("field")
public class CounterAlertDestination extends BaseDatabean<CounterAlertDestinationKey,CounterAlertDestination>{
		
	/********************* fields ***********************************/
	@Id
	private CounterAlertDestinationKey key;	
	
	/******************* constructors *****************************************/
	CounterAlertDestination(){
		this.key = new CounterAlertDestinationKey(null, null, null);
	}
	
	public CounterAlertDestination(CounterAlertDestinationKey key){
		this.key = key;
	}
	
	public static class F{
		public static final String key = "key";
	}
	
	@Override
	public Class<CounterAlertDestinationKey> getKeyClass(){
		return CounterAlertDestinationKey.class;
	}
	
	@Override
	public CounterAlertDestinationKey getKey(){
		return key;
	}
	
	public void setKey(CounterAlertDestinationKey key){
		this.key = key;
	}
	
	@Override
	public List<Field<?>> getNonKeyFields(){
		List<Field<?>> fields = FieldTool.createList();
		return fields;
	}
	
	/***************************** MySQL fielder ******************************/	
	public static class CounterAlertDestinationFielder extends
			BaseDatabeanFielder<CounterAlertDestinationKey,CounterAlertDestination>{
		public CounterAlertDestinationFielder(){}
		@Override
		public Class<? extends Fielder<CounterAlertDestinationKey>> getKeyFielderClass(){
			return CounterAlertDestinationKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(CounterAlertDestination databean){
			return databean.getNonKeyFields();
		}
		
		@Override
		public Map<String,List<Field<?>>> getIndexes(CounterAlertDestination counterAlertDestination){
			Map<String,List<Field<?>>> indexesByName = MapTool.createTreeMap();
			indexesByName.put(CounterAlertDestinationKey.F.notificationDestination,
					new CounterAlertDestinationByNotificationDestinationLookup(null).getFields());
			return indexesByName;
		}
	}
	
	/******************************** indexes / lookup ******************************/
	public static class CounterAlertDestinationByNotificationDestinationLookup extends
			BaseLookup<CounterAlertDestinationKey>{
		private String notificationDestination;
		public CounterAlertDestinationByNotificationDestinationLookup(String notificationDestination){
			this.notificationDestination = notificationDestination;
		}
		@Override
		public List<Field<?>> getFields(){
			return FieldTool.createList(new StringField(CounterAlertDestinationKey.F.notificationDestination,
					notificationDestination, CounterAlertDestinationKey.LENGTH_notificationDestination));
		}
	}

	public static class CounterAlertDestinationByCounterAlertIdLookup extends BaseLookup<CounterAlertDestinationKey>{
		private Long counterAlertId;
		public CounterAlertDestinationByCounterAlertIdLookup(Long counterAlertId){
			this.counterAlertId = counterAlertId;
		}
		@Override
		public List<Field<?>> getFields(){
			return FieldTool.createList( new LongField(CounterAlertDestinationKey.F.counterAlertId, counterAlertId));
		}
	}
	
}
