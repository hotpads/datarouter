package com.hotpads.datarouter.client.imp.kinesis;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.serialize.StringDatabeanCodec;
import com.hotpads.datarouter.serialize.codec.FlatKeyJsonDatabeanCodec;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;

public class KinesisRecord extends BaseDatabean<KinesisRecordKey,KinesisRecord>{

	private KinesisRecordKey key;
	private String recordData;

	private static class FieldKeys{
		private static final StringFieldKey recordData = new StringFieldKey("recordData").withColumnName("RecordData");
	}

	public static class KinesisRecordFielder extends BaseDatabeanFielder<KinesisRecordKey,KinesisRecord>{

		public KinesisRecordFielder(){
			super(KinesisRecordKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(KinesisRecord snsMessage){
			return Arrays.asList(new StringField(FieldKeys.recordData, snsMessage.recordData));
		}

		@Override
		public Class<? extends StringDatabeanCodec> getStringDatabeanCodecClass(){
			return FlatKeyJsonDatabeanCodec.class;
		}

	}

	public KinesisRecord(){
		this.key = new KinesisRecordKey();
	}

	@Override
	public Class<KinesisRecordKey> getKeyClass(){
		return KinesisRecordKey.class;
	}

	@Override
	public KinesisRecordKey getKey(){
		return key;
	}

	public String getRecordData(){
		return recordData;
	}

}