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

import io.datarouter.model.field.Field;
import io.datarouter.model.field.codec.UlidToStringCodec;
import io.datarouter.model.field.imp.StringEncodedField;
import io.datarouter.model.field.imp.StringEncodedFieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;
import io.datarouter.types.Ulid;

public class HttpRequestRecordKey extends BaseRegularPrimaryKey<HttpRequestRecordKey>{

	private Ulid id;

	public static class FieldKeys{
		public static final StringEncodedFieldKey<Ulid> id = new StringEncodedFieldKey<>("id", new UlidToStringCodec());
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new StringEncodedField<>(FieldKeys.id, id));
	}

	public HttpRequestRecordKey(){
	}

	public HttpRequestRecordKey(Ulid id){
		this.id = id;
	}

	public Ulid getId(){
		return id;
	}

}
