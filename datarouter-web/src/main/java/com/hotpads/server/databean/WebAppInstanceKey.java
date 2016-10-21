package com.hotpads.server.databean;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

public class WebAppInstanceKey extends BasePrimaryKey<WebAppInstanceKey> {

	/** fields ****************************************************************/

	private String webAppName;
	private String serverName;

	/** columns ***************************************************************/

	public static class FieldKeys {
		public static final StringFieldKey
			webAppName = new StringFieldKey("webAppName"),
			serverName = new StringFieldKey("serverName");
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
				new StringField(FieldKeys.webAppName, webAppName),
				new StringField(FieldKeys.serverName, serverName));
	}

	/** construct *************************************************************/

	@SuppressWarnings("unused")//for reflection
	private WebAppInstanceKey(){
	}

	public WebAppInstanceKey(String webAppName, String serverName){
		this.webAppName = webAppName;
		this.serverName = serverName;
	}

	/** get/set ***************************************************************/

	public String getWebAppName(){
		return webAppName;
	}

	public String getServerName(){
		return serverName;
	}

}
