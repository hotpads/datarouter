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
package io.datarouter.websocket.storage.session;

import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.encoding.FieldGeneratorType;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.positive.UInt63Field;
import io.datarouter.model.field.imp.positive.UInt63FieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;

public class WebSocketSessionKey extends BaseRegularPrimaryKey<WebSocketSessionKey>{

	private String userToken;
	private Long id;

	public static class FieldKeys{
		public static final StringFieldKey userToken = new StringFieldKey("userToken");
		public static final UInt63FieldKey id = new UInt63FieldKey("id")
				.withFieldGeneratorType(FieldGeneratorType.RANDOM);
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
			new StringField(FieldKeys.userToken, userToken),
			new UInt63Field(FieldKeys.id, id));
	}

	public WebSocketSessionKey(){
		this(null, null);
	}

	public WebSocketSessionKey(String userToken, Long id){
		this.userToken = userToken;
		this.id = id;
	}

	public String getUserToken(){
		return userToken;
	}

	public Long getId(){
		return id;
	}

}
