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
package io.datarouter.auth.storage.user.permissionrequest;

import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.codec.MilliTimeFieldCodec;
import io.datarouter.model.field.imp.comparable.LongEncodedField;
import io.datarouter.model.field.imp.comparable.LongEncodedFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;
import io.datarouter.types.MilliTime;

public class PermissionRequestKey extends BaseRegularPrimaryKey<PermissionRequestKey>{

	private Long userId;
	private MilliTime requestTime;

	public PermissionRequestKey(){
	}

	public PermissionRequestKey(Long userId, MilliTime requestTime){
		this.userId = userId;
		this.requestTime = requestTime;
	}

	public static class FieldKeys{
		public static final LongFieldKey userId = new LongFieldKey("userId");
		public static final LongEncodedFieldKey<MilliTime> requestTime = new LongEncodedFieldKey<>("requestTime",
				new MilliTimeFieldCodec());
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new LongField(FieldKeys.userId, userId),
				new LongEncodedField<>(FieldKeys.requestTime, requestTime));
	}

	public Long getUserId(){
		return userId;
	}

	public void setUserId(Long userId){
		this.userId = userId;
	}

	public MilliTime getRequestTime(){
		return requestTime;
	}

}
