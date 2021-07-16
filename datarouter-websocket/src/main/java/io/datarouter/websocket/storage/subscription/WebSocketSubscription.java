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
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class WebSocketSubscription extends BaseDatabean<WebSocketSubscriptionKey,WebSocketSubscription>{

	private String comment;

	public static class FieldKeys{
		public static final StringFieldKey comment = new StringFieldKey("comment");
	}

	public static class WebSocketSubscriptionFielder
	extends BaseDatabeanFielder<WebSocketSubscriptionKey,WebSocketSubscription>{

		public WebSocketSubscriptionFielder(){
			super(WebSocketSubscriptionKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(WebSocketSubscription webSocketSubscription){
			return List.of(new StringField(FieldKeys.comment, webSocketSubscription.comment));
		}

	}

	public WebSocketSubscription(){
		super(new WebSocketSubscriptionKey());
	}

	public WebSocketSubscription(WebSocketSubscriptionKey key, String comment){
		super(key);
		this.comment = comment;
	}

	@Override
	public Supplier<WebSocketSubscriptionKey> getKeySupplier(){
		return WebSocketSubscriptionKey::new;
	}

	public String getComment(){
		return comment;
	}

	public void setComment(String comment){
		this.comment = comment;
	}

}
