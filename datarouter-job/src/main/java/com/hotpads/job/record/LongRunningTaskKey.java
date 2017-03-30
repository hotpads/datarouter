package com.hotpads.job.record;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.field.imp.custom.LongDateField;
import com.hotpads.datarouter.storage.field.imp.custom.LongDateFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.job.trigger.Job;

public class LongRunningTaskKey extends BasePrimaryKey<LongRunningTaskKey>{

	private Date triggerTime;
	private String jobClass;
	private String serverName;

	public static class FieldKeys{
		public static final LongDateFieldKey triggerTime = new LongDateFieldKey("triggerTime");
		public static final StringFieldKey jobClass = new StringFieldKey("jobClass");
		public static final StringFieldKey serverName = new StringFieldKey("serverName");
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
				new StringField(FieldKeys.jobClass, jobClass),
				new LongDateField(FieldKeys.triggerTime, triggerTime),
				new StringField(FieldKeys.serverName, serverName));
	}

	/****************** construct ************************/

	public LongRunningTaskKey(){
	}

	public LongRunningTaskKey(String jobClass, Date triggerTime, String serverName){
		this.jobClass = jobClass;
		this.triggerTime = triggerTime;
		this.serverName = serverName;
	}

	public LongRunningTaskKey(String jobClass, String serverName){
		this(jobClass, null, serverName);
	}

	public LongRunningTaskKey(String jobClass, Date triggerTime){
		this(jobClass, triggerTime, null);
	}

	public LongRunningTaskKey(Class<? extends Job> jobClass){
		this(jobClass.getSimpleName(), null, null);
	}

	/******************* get/set ***************************/

	public Date getTriggerTime(){
		return triggerTime;
	}

	public void setTriggerTime(Date triggerTime){
		this.triggerTime = triggerTime;
	}

	public String getJobClass(){
		return jobClass;
	}

	public String getServerName(){
		return serverName;
	}
}
