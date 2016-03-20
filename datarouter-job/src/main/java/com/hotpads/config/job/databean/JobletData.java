package com.hotpads.config.job.databean;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.serialize.fielder.Fielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;

public class JobletData extends BaseDatabean<JobletDataKey,JobletData>{
	
	private JobletDataKey key;
	private String data;
	private Long created;
	
	public static class F{
		public static String
			data = "data",
			created = "created";
	}
	
	public static class JobletDataFielder extends BaseDatabeanFielder<JobletDataKey, JobletData>{
		@Override
		public Class<? extends Fielder<JobletDataKey>> getKeyFielderClass(){
			return JobletDataKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(JobletData d){
			return FieldTool.createList(
					new StringField(F.data, d.data, MySqlColumnType.INT_LENGTH_LONGTEXT),
					new LongField(F.created, d.created));
		}
	}
	
	public JobletData() {
		this.created = System.currentTimeMillis();
		this.key = new JobletDataKey();
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
