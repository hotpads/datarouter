package com.hotpads.logging.databean;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
public class FileAppenderKey extends BasePrimaryKey<FileAppenderKey> {

	/** fields ****************************************************************/

	private String name;

	/** columns ***************************************************************/

	private static class F {
		private static final String
		name = "name";
	}

	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new StringField(F.name, name, MySqlColumnType.MAX_LENGTH_VARCHAR));
	}

	/** construct *************************************************************/

	FileAppenderKey(){}

	public FileAppenderKey(String name){
		this.name = name;
	}

	/** get/set ***************************************************************/

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

}