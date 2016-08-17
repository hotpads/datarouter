package com.hotpads.handler.user.session;

import java.util.List;


import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
public class BaseDatarouterSessionDatabeanKey<
		PK extends BaseDatarouterSessionDatabeanKey<PK>>
extends BasePrimaryKey<PK>{

	/************** fields ************************/

	private String sessionToken;

	public class F{
		private static final String
			sessionToken = "sessionToken";
	}

	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
			new StringField(F.sessionToken, sessionToken, MySqlColumnType.MAX_LENGTH_VARCHAR));
	}


	/**************** construct *********************/

	public BaseDatarouterSessionDatabeanKey(){
	}

	public BaseDatarouterSessionDatabeanKey(String sessionToken){
		this.sessionToken = sessionToken;
	}


	/**************** get/set **********************/

	public void setSessionToken(String sessionToken){
		this.sessionToken = sessionToken;
	}

	public String getSessionToken(){
		return sessionToken;
	}

}
