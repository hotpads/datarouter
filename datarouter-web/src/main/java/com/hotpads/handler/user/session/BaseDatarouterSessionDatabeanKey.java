package com.hotpads.handler.user.session;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.datarouter.util.core.DrListTool;

public class BaseDatarouterSessionDatabeanKey<
		PK extends BaseDatarouterSessionDatabeanKey<PK>>
extends BasePrimaryKey<PK>{

	/************** fields ************************/

	private String sessionToken;

	public static class FieldKeys{
		public static final StringFieldKey sessionToken = new StringFieldKey("sessionToken");
	}

	@Override
	public List<Field<?>> getFields(){
		return DrListTool.createArrayList(new StringField(FieldKeys.sessionToken, sessionToken));
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
