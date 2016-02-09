package com.hotpads.websocket.session;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.field.imp.custom.LongDateField;
import com.hotpads.datarouter.storage.field.imp.custom.LongDateFieldKey;


/** CREATE SCRIPT
com.hotpads.websocket.session.WebSocketSession{
  PK{
    StringField userToken,
    StringField id
  }
  LongDateField openingDate,
  StringField serverName,
}

*/
public class WebSocketSession extends BaseDatabean<WebSocketSessionKey,WebSocketSession>{

	private WebSocketSessionKey key;

	private Date openingDate;
	private String serverName;

	public static class FieldKeys{
		public static final LongDateFieldKey openingDate = new LongDateFieldKey("openingDate");
		public static final StringFieldKey serverName = new StringFieldKey("serverName");
	}

	public static class WebSocketSessionFielder
	extends BaseDatabeanFielder<WebSocketSessionKey, WebSocketSession>{
		public WebSocketSessionFielder(){
			super(WebSocketSessionKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(WebSocketSession webSocketSession){
			return Arrays.asList(
				new LongDateField(FieldKeys.openingDate, webSocketSession.openingDate),
				new StringField(FieldKeys.serverName, webSocketSession.serverName));
		}

	}

	@SuppressWarnings("unused") // used by datarouter reflection
	private WebSocketSession(){
		this.key = new WebSocketSessionKey();
	}

	public WebSocketSession(String userToken, String serverName){
		this.key = new WebSocketSessionKey(userToken);
		this.openingDate = new Date();
		this.serverName = serverName;
	}

	@Override
	public Class<WebSocketSessionKey> getKeyClass(){
		return WebSocketSessionKey.class;
	}

	@Override
	public WebSocketSessionKey getKey(){
		return key;
	}

	public Date getOpeningDate(){
		return openingDate;
	}

	public String getServerName(){
		return serverName;
	}

}
