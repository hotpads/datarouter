package com.hotpads.job.record;

import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.custom.LongDateField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
public class LongRunningTaskKey extends BasePrimaryKey<LongRunningTaskKey>{

	public static final int LENGTH_jobClass = MySqlColumnType.MAX_LENGTH_VARCHAR;
	public static final int LENGTH_serverName = MySqlColumnType.MAX_LENGTH_VARCHAR;
	
	/****************** fields ***********************/
	
	private Date triggerTime;
	private String jobClass;
	private String serverName;
	
	public static class F{
		public static final String
			triggerTime = "triggerTime",
			jobClass = "jobClass",
			serverName = "serverName";
	}

	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new LongDateField(F.triggerTime, triggerTime),
				new StringField(F.jobClass, jobClass, LENGTH_jobClass),
				new StringField(F.serverName, serverName, LENGTH_serverName));
	}
	
	
	/****************** construct ************************/
	
	LongRunningTaskKey(){
	}
	
	public LongRunningTaskKey(Date triggerTime, String jobClass, String serverName) {
		this.triggerTime = triggerTime;
		this.jobClass = jobClass;
		this.serverName = serverName;
	}
	
	public LongRunningTaskKey(String jobClass, String serverName) {
		this.jobClass = jobClass;
		this.serverName = serverName;
	}
	
	/******************* get/set ***************************/

	public Date getTriggerTime() {
		return triggerTime;
	}


	public void setTriggerTime(Date triggerTime) {
		this.triggerTime = triggerTime;
	}


	public String getJobClass() {
		return jobClass;
	}


	public void setJobClass(String jobClass) {
		this.jobClass = jobClass;
	}


	public String getServerName() {
		return serverName;
	}


	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
}
