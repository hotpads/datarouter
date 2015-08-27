package com.hotpads.websocket.session;

import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.custom.LongDateField;


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
public class WebSocketSession extends BaseDatabean<WebSocketSessionKey,WebSocketSession> {

	private WebSocketSessionKey key;

	private Date openingDate;
	private String serverName;

	public static class F {
		public static final String
			openingDate = "openingDate",
			serverName = "serverName";
	}

	public static class WebSocketSessionFielder
	extends BaseDatabeanFielder<WebSocketSessionKey, WebSocketSession>{
		public WebSocketSessionFielder(){
		}

		@Override
		public Class<WebSocketSessionKey> getKeyFielderClass() {
			return WebSocketSessionKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(WebSocketSession webSocketSession){
			return FieldTool.createList(
				new LongDateField(F.openingDate, webSocketSession.openingDate),
				new StringField(F.serverName, webSocketSession.serverName, MySqlColumnType.MAX_LENGTH_VARCHAR));
		}

	}

	@SuppressWarnings("unused") //used by datarouter reflection
	private WebSocketSession(){
		this.key = new WebSocketSessionKey();
	}

	public WebSocketSession(String userToken, String serverName){
		this.key = new WebSocketSessionKey(userToken);
		this.openingDate = new Date();
		this.serverName = serverName;
	}

	@Override
	public Class<WebSocketSessionKey> getKeyClass() {
		return WebSocketSessionKey.class;
	}

	@Override
	public WebSocketSessionKey getKey() {
		return key;
	}

	public Date getOpeningDate(){
		return openingDate;
	}

	public String getServerName(){
		return serverName;
	}

}
