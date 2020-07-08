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
package io.datarouter.auth.storage.userhistory;

import java.util.Date;
import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.DateField;
import io.datarouter.model.field.imp.DateFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;

public class DatarouterUserHistoryKey extends BaseRegularPrimaryKey<DatarouterUserHistoryKey>{

	private Long userId;
	private Date time;

	public DatarouterUserHistoryKey(){
	}

	public DatarouterUserHistoryKey(Long userId, Date time){
		this.userId = userId;
		this.time = time;
	}

	public static class FieldKeys{
		public static final LongFieldKey userId = new LongFieldKey("userId");
		public static final DateFieldKey time = new DateFieldKey("time");
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new LongField(FieldKeys.userId, userId),
				new DateField(FieldKeys.time, time));
	}

	public Long getUserId(){
		return userId;
	}

	public void setUserId(Long userId){
		this.userId = userId;
	}

	public Date getTime(){
		return time;
	}

	public void setTime(Date time){
		this.time = time;
	}

}
