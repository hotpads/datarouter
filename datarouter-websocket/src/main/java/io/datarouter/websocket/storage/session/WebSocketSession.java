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

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.codec.MilliTimeFieldCodec;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.LongEncodedField;
import io.datarouter.model.field.imp.comparable.LongEncodedFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.types.MilliTime;

public class WebSocketSession extends BaseDatabean<WebSocketSessionKey,WebSocketSession>{

	private String mode;
	private MilliTime openingDate;
	private String serverName;

	public static class FieldKeys{
		public static final StringFieldKey mode = new StringFieldKey("mode");
		public static final LongEncodedFieldKey<MilliTime> openingDate = new LongEncodedFieldKey<>(
				"openingDate", new MilliTimeFieldCodec());
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
					new StringField(FieldKeys.mode, webSocketSession.mode),
					new LongEncodedField<>(FieldKeys.openingDate, webSocketSession.openingDate),
					new StringField(FieldKeys.serverName, webSocketSession.serverName));
		}

	}

	public WebSocketSession(){
		super(new WebSocketSessionKey());
	}

	public WebSocketSession(WebSocketSessionKey key, String mode, MilliTime openingDate, String serverName){
		super(key);
		this.mode = mode;
		this.openingDate = openingDate;
		this.serverName = serverName;
	}

	@Override
	public Supplier<WebSocketSessionKey> getKeySupplier(){
		return WebSocketSessionKey::new;
	}

	public String getMode(){
		return mode;
	}

	public void setMode(String mode){
		this.mode = mode;
	}

	public MilliTime getOpeningDate(){
		return openingDate;
	}

	public String getServerName(){
		return serverName;
	}

}
