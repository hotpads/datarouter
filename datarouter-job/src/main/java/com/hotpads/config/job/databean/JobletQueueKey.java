/**
 *
 */
package com.hotpads.config.job.databean;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
@Embeddable
public class JobletQueueKey extends BasePrimaryKey<JobletQueueKey>{

	@Column(length = MySqlColumnType.MAX_KEY_LENGTH_UTF8MB4)
	protected String id;

	@Override
	public List<Field<?>> getFields() {
		return FieldTool.createList(
				new StringField(JobletQueue.COL_id, this.id, MySqlColumnType.MAX_KEY_LENGTH_UTF8MB4));
	}


	JobletQueueKey(){
	}

	public JobletQueueKey(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}


}