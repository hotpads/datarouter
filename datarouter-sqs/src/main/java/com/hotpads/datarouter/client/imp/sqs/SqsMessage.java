package com.hotpads.datarouter.client.imp.sqs;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.serialize.StringDatabeanCodec;
import com.hotpads.datarouter.serialize.codec.FlatKeyJsonDatabeanCodec;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;

public class SqsMessage extends BaseDatabean<SqsMessageKey,SqsMessage>{

	private SqsMessageKey key;
	private String message;

	private static class FieldKeys{
		private static final StringFieldKey message = new StringFieldKey("message").withColumnName("Message");
	}

	public static class SqsMessageFielder extends BaseDatabeanFielder<SqsMessageKey,SqsMessage>{

		public SqsMessageFielder(){
			super(SqsMessageKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(SqsMessage sqsMessage){
			return Arrays.asList(new StringField(FieldKeys.message, sqsMessage.message));
		}

		@Override
		public Class<? extends StringDatabeanCodec> getStringDatabeanCodecClass(){
			return FlatKeyJsonDatabeanCodec.class;
		}

	}

	public SqsMessage(){
		this.key = new SqsMessageKey();
	}

	public SqsMessage(String message){
		this();
		this.message = message;
	}

	@Override
	public Class<SqsMessageKey> getKeyClass(){
		return SqsMessageKey.class;
	}

	@Override
	public SqsMessageKey getKey(){
		return key;
	}

	public String getMessage(){
		return message;
	}

}
