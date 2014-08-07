package com.hotpads.logging.databean;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;

/** CREATE SCRIPT
com.hotpads.logging.FileAppender{
  PK{
    StringField name
  }
  StringField layout,
  StringField fileName

}
 */

public class FileAppender extends BaseDatabean<FileAppenderKey,FileAppender> {

	/** fields ****************************************************************/

	private FileAppenderKey key;

	private String layout;
	private String fileName;


	/** columns ***************************************************************/

	private static class F {
		private static final String
		layout = "layout",
		fileName = "fileName";
	}

	/** fielder ***************************************************************/

	public static class FileAppenderFielder extends BaseDatabeanFielder<FileAppenderKey,FileAppender>{

		private FileAppenderFielder(){}

		@Override
		public Class<FileAppenderKey> getKeyFielderClass() {
			return FileAppenderKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(FileAppender d){
			return FieldTool.createList(
					new StringField(F.layout, d.layout, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.fileName, d.fileName, MySqlColumnType.MAX_LENGTH_VARCHAR));
		}

	}

	/** construct *************************************************************/

	private FileAppender(){
		this.key = new FileAppenderKey();
	}

	public FileAppender(String name, String layout, String fileName){
		this.key = new FileAppenderKey(name);
		this.layout = layout;
		this.fileName = fileName;
	}

	/** databean **************************************************************/

	@Override
	public Class<FileAppenderKey> getKeyClass() {
		return FileAppenderKey.class;
	}

	@Override
	public FileAppenderKey getKey() {
		return key;
	}

	/** get/set ***************************************************************/

	public void setKey(FileAppenderKey key) {
		this.key = key;
	}

	public String getLayout(){
		return layout;
	}

	public void setLayout(String layout){
		this.layout = layout;
	}

	public String getFileName(){
		return fileName;
	}

	public void setFileName(String fileName){
		this.fileName = fileName;
	}

	public String getName(){
		return key.getName();
	}

	public void setName(String name){
		this.key.setName(name);
	}

}
