package com.hotpads.websocket.session;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63FieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
public class WebSocketSessionKey extends BasePrimaryKey<WebSocketSessionKey> {

	private String userToken;
	private Long id;

	public static class FieldKeys{
		public static final StringFieldKey userToken = new StringFieldKey("userToken");
		public static final UInt63FieldKey id = new UInt63FieldKey("id", FieldGeneratorType.RANDOM);
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
			new StringField(FieldKeys.userToken, userToken),
			new UInt63Field(FieldKeys.id, id));
	}

	public WebSocketSessionKey(){
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
