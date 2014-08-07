package com.hotpads.logging;

import java.util.List;

import org.apache.logging.log4j.Level;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.array.DelimitedStringArrayField;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;

/** CREATE SCRIPT
com.hotpads.logging.LoggerConfig{
  PK{
    StringField name
  }
  BooleanField additive,
  DelimitedStringArrayField appendersRef
}
 */
public class LoggerConfig extends BaseDatabean<LoggerConfigKey,LoggerConfig> {

	/** fields ****************************************************************/

	private LoggerConfigKey key;

	private LoggingLevel level;
	private Boolean additive;
	private List<String> appendersRef;


	/** columns ***************************************************************/

	private static class F {
		private static final String
		level = "level",
		additive = "additive",
		appendersRef = "appendersRef";
	}

	/** fielder ***************************************************************/

	public static class LoggerConfigFielder extends BaseDatabeanFielder<LoggerConfigKey, LoggerConfig>{

		private LoggerConfigFielder(){}

		@Override
		public Class<LoggerConfigKey> getKeyFielderClass() {
			return LoggerConfigKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(LoggerConfig d){
			return FieldTool.createList(
					new StringEnumField<LoggingLevel>(LoggingLevel.class, F.level, d.level, LoggingLevel.getSqlSize()),
					new BooleanField(F.additive, d.additive),
					new DelimitedStringArrayField(F.appendersRef, ",", d.appendersRef));
		}

	}

	/** construct *************************************************************/

	private LoggerConfig(){
		this.key = new LoggerConfigKey();
	}

	public LoggerConfig(String name, LoggingLevel level, boolean additive, List<String> appendersRef){
		this.key = new LoggerConfigKey(name);
		this.level = level;
		this.additive = additive;
		this.appendersRef = appendersRef;
	}

	public LoggerConfig(String name, Level level, boolean additive, List<String> appendersRef){
		this(name, LoggingLevel.fromString(level.name()), additive, appendersRef);
	}

	// public LoggerConfig(String name, String level, boolean additive, String... appendersRef){
	// this(name, LoggingLevel.fromString(level), additive, appendersRef);
	// }

	/** databean **************************************************************/

	@Override
	public Class<LoggerConfigKey> getKeyClass() {
		return LoggerConfigKey.class;
	}

	@Override
	public LoggerConfigKey getKey() {
		return key;
	}

	/** get/set ***************************************************************/

	public void setKey(LoggerConfigKey key) {
		this.key = key;
	}

	public Boolean getAdditive(){
		return additive;
	}

	public void setAdditive(Boolean additive){
		this.additive = additive;
	}

	public List<String> getAppendersRef(){
		return appendersRef;
	}

	public void setAppendersRef(List<String> appendersRef){
		this.appendersRef = appendersRef;
	}

	public LoggingLevel getLevel(){
		return level;
	}

	public void setLevel(LoggingLevel level){
		this.level = level;
	}

	public String getName(){
		return key.getName();
	}

	public void setName(String name){
		this.key.setName(name);
	}

}
