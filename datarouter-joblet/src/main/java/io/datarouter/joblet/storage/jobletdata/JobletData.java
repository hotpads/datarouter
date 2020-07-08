/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.joblet.storage.jobletdata;

import java.util.List;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.util.CommonFieldSizes;

public class JobletData extends BaseDatabean<JobletDataKey,JobletData>{

	private String data;
	private Long created;

	public static class FieldKeys{
		public static final StringFieldKey data = new StringFieldKey("data")
				.withSize(CommonFieldSizes.INT_LENGTH_LONGTEXT);
		public static final LongFieldKey created = new LongFieldKey("created");
	}


	public static class JobletDataFielder extends BaseDatabeanFielder<JobletDataKey,JobletData>{

		public JobletDataFielder(){
			super(JobletDataKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(JobletData databean){
			return List.of(
					new StringField(FieldKeys.data, databean.data),
					new LongField(FieldKeys.created, databean.created));
		}

	}

	public JobletData(){
		this(null);
	}

	public JobletData(String data){
		super(new JobletDataKey());
		this.created = System.currentTimeMillis();
		this.data = data;
	}

	@Override
	public Class<JobletDataKey> getKeyClass(){
		return JobletDataKey.class;
	}

	public String getData(){
		return data;
	}

	public Long getCreated(){
		return created;
	}

}
