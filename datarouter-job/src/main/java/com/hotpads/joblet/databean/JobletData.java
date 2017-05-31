package com.hotpads.joblet.databean;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.client.imp.mysql.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.MySqlRowFormat;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongFieldKey;

public class JobletData extends BaseDatabean<JobletDataKey,JobletData>{

	private JobletDataKey key;
	private String data;
	private Long created;


	public static class FieldKeys{
		public static final StringFieldKey data = new StringFieldKey("data")
				.withSize(MySqlColumnType.INT_LENGTH_LONGTEXT);
		public static final LongFieldKey created = new LongFieldKey("created");
	}


	public static class JobletDataFielder extends BaseDatabeanFielder<JobletDataKey, JobletData>{
		public JobletDataFielder(){
			super(JobletDataKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(JobletData databean){
			return Arrays.asList(
					new StringField(FieldKeys.data, databean.data),
					new LongField(FieldKeys.created, databean.created));
		}

		@Override
		public MySqlRowFormat getRowFormat(){
			return MySqlRowFormat.COMPACT;
		}
	}


	public JobletData(){
		this(null);
	}

	public JobletData(String data){
		this.key = new JobletDataKey();
		this.created = System.currentTimeMillis();
		this.data = data;
	}

	@Override
	public Class<JobletDataKey> getKeyClass(){
		return JobletDataKey.class;
	}

	@Override
	public JobletDataKey getKey() {
		return key;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public Long getCreated() {
		return created;
	}

	public void setCreated(Long created) {
		this.created = created;
	}

	public Long getId() {
		return key.getId();
	}

}
