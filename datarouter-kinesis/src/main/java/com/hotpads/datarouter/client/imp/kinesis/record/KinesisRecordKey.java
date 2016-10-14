package com.hotpads.datarouter.client.imp.kinesis.record;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

public class KinesisRecordKey extends BasePrimaryKey<KinesisRecordKey>{

	private String sequenceNumber;

	private static class FieldKeys{
		private static final StringFieldKey sequenceNumber = new StringFieldKey("sequenceNumber").withColumnName(
				"SequenceNumber");
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(new StringField(FieldKeys.sequenceNumber, sequenceNumber));
	}

}
