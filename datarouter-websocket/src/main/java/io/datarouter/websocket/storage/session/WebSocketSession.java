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
package io.datarouter.websocket.storage.session;

import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.codec.DateToLongFieldCodec;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.LongEncodedField;
import io.datarouter.model.field.imp.comparable.LongEncodedFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class WebSocketSession extends BaseDatabean<WebSocketSessionKey,WebSocketSession>{

	private Date openingDate;
	private String serverName;

	public static class FieldKeys{
		public static final LongEncodedFieldKey<Date> openingDate = new LongEncodedFieldKey<>(
				"openingDate", new DateToLongFieldCodec());
		public static final StringFieldKey serverName = new StringFieldKey("serverName");
	}

	public static class WebSocketSessionFielder
	extends BaseDatabeanFielder<WebSocketSessionKey,WebSocketSession>{

		public WebSocketSessionFielder(){
			super(WebSocketSessionKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(WebSocketSession webSocketSession){
			return List.of(
				new LongEncodedField<>(FieldKeys.openingDate, webSocketSession.openingDate),
				new StringField(FieldKeys.serverName, webSocketSession.serverName));
		}

	}

	public WebSocketSession(){
		this(null, null, null, null);
	}

	public WebSocketSession(String userToken, Long id, Date openingDate, String serverName){
		super(new WebSocketSessionKey(userToken, id));
		this.openingDate = openingDate;
		this.serverName = serverName;
	}

	@Override
	public Supplier<WebSocketSessionKey> getKeySupplier(){
		return WebSocketSessionKey::new;
	}

	public Date getOpeningDate(){
		return openingDate;
	}

	public String getServerName(){
		return serverName;
	}

}
