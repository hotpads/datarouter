package com.hotpads.profile.count.databean;

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
import com.hotpads.datarouter.storage.key.multi.BaseLookup;
import com.hotpads.profile.count.databean.key.CounterAlertDestinationKey;
import com.hotpads.util.core.MapTool;

@SuppressWarnings("serial")
@Entity
@AccessType("field")
public class CounterAlertDestination extends BaseDatabean<CounterAlertDestinationKey,CounterAlertDestination>{
	
	private static final int LEN_COMMENT = MySqlColumnType.MAX_LENGTH_VARCHAR;
	private static final int LEN_EMAILS = MySqlColumnType.MAX_LENGTH_VARCHAR;
	
	/********************* fields ***********************************/
	@Id
	private CounterAlertDestinationKey key;	
	private String texts;
	private String emails;
	
	/******************* constructors *****************************************/
	public CounterAlertDestination(){
		this.key = new CounterAlertDestinationKey();
	}
	
	public CounterAlertDestination(CounterAlertDestinationKey key){
		this.key = key;
	}
	
	public static class F{
		public static final String
		key = "key",
		texts = "texts",
		emails = "emails"
		;
	}
	
	@Override
	public boolean isFieldAware(){
		return true;
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
	
	public static class CounterAlertDestinationFielder extends BaseDatabeanFielder<CounterAlertDestinationKey, CounterAlertDestination>{
		public CounterAlertDestinationFielder(){}
		@Override
		public Class<? extends Fielder<CounterAlertDestinationKey>> getKeyFielderClass(){
			return CounterAlertDestinationKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(CounterAlertDestination databean){
			List<Field<?>> fields = FieldTool.createList(
					new StringField(F.texts, databean.texts, LEN_COMMENT), 
					new StringField(F.emails, databean.emails, LEN_EMAILS)
					);

			return fields;
		}
		
		@Override
		public Map<String,List<Field<?>>> getIndexes(CounterAlertDestination counterAlertDestination){
			Map<String,List<Field<?>>> indexesByName = MapTool.createTreeMap();
			indexesByName.put(F.emails, new CounterAlertDestinationByEmailLookup(null).getFields());
			return indexesByName;
		}
	}
	
	/******************************** indexes / lookup ******************************/
	public static class CounterAlertDestinationByEmailLookup extends BaseLookup<CounterAlertDestinationKey>{
		String emails;
		public CounterAlertDestinationByEmailLookup(String emails){
			this.emails = emails;
		}
		@Override
		public List<Field<?>> getFields(){
			return FieldTool.createList( new StringField(F.emails, emails, LEN_EMAILS));
		}
	}

	
	/******************************** getter/setter *************************************/
	public String getTexts(){
		return texts;
	}

	public void setTexts(String comment){
		this.texts = comment;
	}

	public String getEmails(){
		return emails;
	}

	public void setEmails(String emails){
		this.emails = emails;
	}
}
