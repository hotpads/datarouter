/**
 *
 */
package com.hotpads.joblet.databean;

import java.util.Arrays;
import java.util.List;


import com.hotpads.datarouter.client.imp.mysql.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

public class JobletQueueKey extends BasePrimaryKey<JobletQueueKey>{

	private String id;


	public static class FieldKeys{
		public static final StringFieldKey id = new StringFieldKey("id")
				.withSize(MySqlColumnType.MAX_KEY_LENGTH_UTF8MB4);
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
				new StringField(FieldKeys.id, id));
	}


	JobletQueueKey(){
	}

	public JobletQueueKey(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}

	public void setId(String id){
		this.id = id;
	}


}