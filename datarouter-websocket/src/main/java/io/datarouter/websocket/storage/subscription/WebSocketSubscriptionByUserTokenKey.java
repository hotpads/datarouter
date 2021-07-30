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
package io.datarouter.websocket.storage.subscription;

import java.util.List;

import io.datarouter.model.databean.FieldlessIndexEntry;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.positive.UInt63Field;
import io.datarouter.model.key.FieldlessIndexEntryPrimaryKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;
import io.datarouter.websocket.storage.subscription.WebSocketSubscriptionKey.FieldKeys;

public class WebSocketSubscriptionByUserTokenKey
extends BaseRegularPrimaryKey<WebSocketSubscriptionByUserTokenKey>
implements FieldlessIndexEntryPrimaryKey<
		WebSocketSubscriptionByUserTokenKey,
		WebSocketSubscriptionKey,
		WebSocketSubscription>{

	private String userToken;
	private Long webSocketSessionId;
	private String topic;

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new StringField(FieldKeys.userToken, userToken),
				new UInt63Field(FieldKeys.webSocketSessionId, webSocketSessionId),
				new StringField(FieldKeys.topic, topic));
	}

	public WebSocketSubscriptionByUserTokenKey(){
		this(null, null, null);
	}

	public WebSocketSubscriptionByUserTokenKey(String userToken, Long webSocketSessionId, String topic){
		this.userToken = userToken;
		this.webSocketSessionId = webSocketSessionId;
		this.topic = topic;
	}

	@Override
	public WebSocketSubscriptionKey getTargetKey(){
		return new WebSocketSubscriptionKey(topic, userToken, webSocketSessionId);
	}

	@Override
	public FieldlessIndexEntry<WebSocketSubscriptionByUserTokenKey,WebSocketSubscriptionKey,WebSocketSubscription>
			createFromDatabean(WebSocketSubscription target){
		return new FieldlessIndexEntry<>(WebSocketSubscriptionByUserTokenKey::new,
				new WebSocketSubscriptionByUserTokenKey(
						target.getKey().getUserToken(),
						target.getKey().getWebSocketSessionId(),
						target.getKey().getTopic()));
	}

}
