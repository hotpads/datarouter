package com.hotpads.logging;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;


/** CREATE SCRIPT
com.hotpads.logging.ConsoleAppender{
  PK{
    StringField name
  }
  StringField layout,
  StringField target

}
 */
public class ConsoleAppender extends BaseDatabean<ConsoleAppenderKey,ConsoleAppender> {

	/** fields ****************************************************************/

	private ConsoleAppenderKey key;

	private String layout;
	private String target;


	/** columns ***************************************************************/

	private static class F {
		private static final String
		layout = "layout",
		target = "target";
	}

	/** fielder ***************************************************************/

	public static class ConsoleAppenderFielder extends BaseDatabeanFielder<ConsoleAppenderKey,ConsoleAppender>{

		private ConsoleAppenderFielder(){}

		@Override
		public Class<ConsoleAppenderKey> getKeyFielderClass() {
			return ConsoleAppenderKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(ConsoleAppender d){
			return FieldTool.createList(
					new StringField(F.layout, d.layout, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.target, d.target, MySqlColumnType.MAX_LENGTH_VARCHAR));
		}

	}

	/** construct *************************************************************/

	private ConsoleAppender(){
		this.key = new ConsoleAppenderKey();
	}

	public ConsoleAppender(String name, String layout, String target){
		this.key = new ConsoleAppenderKey(name);
		this.layout = layout;
		this.target = target;
	}

	/** databean **************************************************************/

	@Override
	public Class<ConsoleAppenderKey> getKeyClass() {
		return ConsoleAppenderKey.class;
	}

	@Override
	public ConsoleAppenderKey getKey() {
		return key;
	}

	/** get/set ***************************************************************/

	public void setKey(ConsoleAppenderKey key) {
		this.key = key;
	}

	public String getLayout(){
		return layout;
	}

	public void setLayout(String layout){
		this.layout = layout;
	}

	public String getTarget(){
		return target;
	}

	public void setTarget(String target){
		this.target = target;
	}

	public String getName(){
		return key.getName();
	}

	public void setName(String name){
		this.key.setName(name);
	}

}

