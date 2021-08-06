/*
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
package io.datarouter.exception.storage.httprecord;

import java.util.List;

import io.datarouter.model.databean.FieldlessIndexEntry;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.key.FieldlessIndexEntryPrimaryKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;

public class HttpRequestRecordByExceptionRecordIdKey
extends BaseRegularPrimaryKey<HttpRequestRecordByExceptionRecordIdKey>
implements FieldlessIndexEntryPrimaryKey<
		HttpRequestRecordByExceptionRecordIdKey,
		HttpRequestRecordKey,
		HttpRequestRecord>{

	private String exceptionRecordId;
	private String id;

	public HttpRequestRecordByExceptionRecordIdKey(){
	}

	public HttpRequestRecordByExceptionRecordIdKey(String exceptionRecordId, String id){
		this.exceptionRecordId = exceptionRecordId;
		this.id = id;
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new StringField(BaseHttpRequestRecord.FieldKeys.exceptionRecordId, exceptionRecordId),
				new StringField(BaseHttpRequestRecordKey.FieldKeys.id, id));

	}

	@Override
	public HttpRequestRecordKey getTargetKey(){
		return new HttpRequestRecordKey(id);
	}

	@Override
	public FieldlessIndexEntry<HttpRequestRecordByExceptionRecordIdKey,HttpRequestRecordKey,HttpRequestRecord>
			createFromDatabean(HttpRequestRecord target){
		var index = new HttpRequestRecordByExceptionRecordIdKey(target.getExceptionRecordId(), target.getKey().getId());
		return new FieldlessIndexEntry<>(HttpRequestRecordByExceptionRecordIdKey::new, index);
	}

}
