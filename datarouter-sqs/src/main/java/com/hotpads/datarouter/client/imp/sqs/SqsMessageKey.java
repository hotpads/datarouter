package com.hotpads.datarouter.client.imp.sqs;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

public class SqsMessageKey extends BasePrimaryKey<SqsMessageKey>{

	private String messageId;

	public SqsMessageKey(){
	}

	public SqsMessageKey(String messageId){
		this.messageId = messageId;
	}

	private static class FieldKeys{
		private static final StringFieldKey messageId = new StringFieldKey("messageId").withColumnName("MessageId");
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(new StringField(FieldKeys.messageId, messageId));
	}

}
