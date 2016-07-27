package com.hotpads.datarouter.client.imp.sns;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;

public class SnsMessage extends BaseDatabean<SnsMessageKey,SnsMessage>{

	private SnsMessageKey key;
	private String message;

	private static class FieldKeys{
		private static final StringFieldKey message = new StringFieldKey("message").withColumnName("Message");
	}

	public static class SnsMessageFielder extends BaseDatabeanFielder<SnsMessageKey,SnsMessage>{

		public SnsMessageFielder(){
			super(SnsMessageKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(SnsMessage snsMessage){
			return Arrays.asList(new StringField(FieldKeys.message, snsMessage.message));
		}

	}

	public SnsMessage(){
		this.key = new SnsMessageKey();
	}

	@Override
	public Class<SnsMessageKey> getKeyClass(){
		return SnsMessageKey.class;
	}

	@Override
	public SnsMessageKey getKey(){
		return key;
	}

	public String getMessage(){
		return message;
	}

}