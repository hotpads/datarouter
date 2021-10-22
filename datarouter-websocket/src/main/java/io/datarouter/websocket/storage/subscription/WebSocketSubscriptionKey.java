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
package io.datarouter.websocket.storage.subscription;

import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;

public class WebSocketSubscriptionKey extends BaseRegularPrimaryKey<WebSocketSubscriptionKey>{

	private String topic;
	private String userToken;
	private Long webSocketSessionId;

	public static class FieldKeys{
		public static final StringFieldKey topic = new StringFieldKey("topic");
		public static final StringFieldKey userToken = new StringFieldKey("userToken");
		public static final LongFieldKey webSocketSessionId = new LongFieldKey("webSocketSessionId");
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new StringField(FieldKeys.topic, topic),
				new StringField(FieldKeys.userToken, userToken),
				new LongField(FieldKeys.webSocketSessionId, webSocketSessionId));
	}

	public WebSocketSubscriptionKey(){
	}

	public WebSocketSubscriptionKey(String topic, String userToken, Long webSocketSessionId){
		this.topic = topic;
		this.userToken = userToken;
		this.webSocketSessionId = webSocketSessionId;
	}

	public String getTopic(){
		return topic;
	}

	public String getUserToken(){
		return userToken;
	}

	public Long getWebSocketSessionId(){
		return webSocketSessionId;
	}

}
