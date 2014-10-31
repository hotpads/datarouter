package com.hotpads.notification.alias;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
public class SubscriberKey extends BasePrimaryKey<SubscriberKey> {

	/** fields ****************************************************************/

	private NotificationAlias alias;
	private String email;

	/** columns ***************************************************************/

	public static class F {
		public static final String
			alias = "alias",
			email = "email";
	}

	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
			new StringField(F.alias, NotificationAlias.F.name, alias.getName(), MySqlColumnType.MAX_LENGTH_VARCHAR),
			new StringField(F.email, email, MySqlColumnType.MAX_LENGTH_VARCHAR));
	}

	/** construct *************************************************************/

	private SubscriberKey(){
		this(new NotificationAlias(null), null);
	}

	public SubscriberKey(NotificationAlias alias, String email){
		this.alias = alias;
		this.email = email;
	}

	/** get/set ***************************************************************/

	public String getEmail(){
		return email;
	}

	public NotificationAlias getAlias(){
		return alias;
	}

}
