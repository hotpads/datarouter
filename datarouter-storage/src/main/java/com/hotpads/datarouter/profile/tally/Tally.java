package com.hotpads.datarouter.profile.tally;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongFieldKey;

public class Tally extends BaseDatabean<TallyKey, Tally>{

	private TallyKey key;
	private Long tally;

	/**************************** column names *******************************/

	public static class FieldKeys{
		public static final LongFieldKey tally = new LongFieldKey("tally");
	}

	public static class TallyFielder extends BaseDatabeanFielder<TallyKey, Tally>{

		public TallyFielder(){
			super(TallyKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(Tally databean){
			return Arrays.asList(new LongField(FieldKeys.tally, databean.tally));
		}
	}

	/**************************** constructor  *******************************/

	public Tally(){
		this(null);
	}

	public Tally(String id){
		this.key = new TallyKey(id);
	}

	/**************************** databean ***********************************/

	@Override
	public Class<TallyKey> getKeyClass(){
		return TallyKey.class;
	}

	@Override
	public TallyKey getKey(){
		return key;
	}
}