package com.hotpads.websocket.session;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
public class WebSocketSessionKey extends BasePrimaryKey<WebSocketSessionKey> {

	private String userToken;
	private Long id;

	public static class F {
		public static final String
			userToken = "userToken",
			id = "id";
	}

	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
			new StringField(F.userToken, userToken, MySqlColumnType.MAX_LENGTH_VARCHAR),
			new UInt63Field(null, F.id, false, FieldGeneratorType.RANDOM, id));
	}

	WebSocketSessionKey(){
	}

	public WebSocketSessionKey(String userToken){
		this.userToken = userToken;
	}

	public String getUserToken(){
		return userToken;
	}

	public Long getId(){
		return id;
	}

}