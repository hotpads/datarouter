package com.hotpads.notification.alias.databean;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.notification.alias.NotificationAlias;
import com.hotpads.notification.alias.NotificationAlias.F;

@SuppressWarnings("serial")
public class ModeratorKey extends BasePrimaryKey<ModeratorKey> {

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

	private ModeratorKey(){
		this(new NotificationAlias(null), null);
	}

	public ModeratorKey(NotificationAlias alias, String email){
		this.alias = alias;
		this.email = email;
	}

	/** get/set ***************************************************************/

}
