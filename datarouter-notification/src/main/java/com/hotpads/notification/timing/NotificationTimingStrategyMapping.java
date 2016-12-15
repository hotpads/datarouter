package com.hotpads.notification.timing;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;

public class NotificationTimingStrategyMapping extends
		BaseDatabean<NotificationTimingStrategyMappingKey,NotificationTimingStrategyMapping>{
	private NotificationTimingStrategyMappingKey key;

	private String timingStrategy;

	public NotificationTimingStrategyMapping(){
		this.key = new NotificationTimingStrategyMappingKey();
	}

	public NotificationTimingStrategyMapping(String type, String channelPrefix, String timingStrategy){
		this.key = new NotificationTimingStrategyMappingKey(type, channelPrefix);
		this.timingStrategy = timingStrategy;
	}

	@Override
	public Class<NotificationTimingStrategyMappingKey> getKeyClass(){
		return NotificationTimingStrategyMappingKey.class;
	}

	@Override
	public NotificationTimingStrategyMappingKey getKey(){
		return key;
	}

	public String getTimingStrategy(){
		return timingStrategy;
	}

	public void setTimingStrategy(String timingStrategy){
		this.timingStrategy = timingStrategy;
	}

	public static class FieldKeys{
		public static final StringFieldKey timingStrategy = new StringFieldKey("timingStrategy");
	}

	public static class NotificationTimingStrategyMappingFielder extends
			BaseDatabeanFielder<NotificationTimingStrategyMappingKey,NotificationTimingStrategyMapping>{
		public NotificationTimingStrategyMappingFielder(){
			super(NotificationTimingStrategyMappingKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(NotificationTimingStrategyMapping databean){
			return Arrays.asList(new StringField(FieldKeys.timingStrategy, databean.timingStrategy));
		}
	}
}
