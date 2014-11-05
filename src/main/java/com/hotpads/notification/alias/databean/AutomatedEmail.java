package com.hotpads.notification.alias.databean;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;


/** CREATE SCRIPT
com.hotpads.notification.alias.AutomatedEmail{
  PK{
    LongField reverseCreatedMs,
    LongField nanoTime
  }
  StringField subject,
  StringField content

}

*/
public class AutomatedEmail extends BaseDatabean<AutomatedEmailKey,AutomatedEmail> {

	/** fields ****************************************************************/

	private AutomatedEmailKey key;

	private String subject;
	private String content;


	/** columns ***************************************************************/

	public static class F {
		public static final String
			subject = "subject",
			content = "content";
	}

	/** fielder ***************************************************************/

	public static class AutomatedEmailFielder extends BaseDatabeanFielder<AutomatedEmailKey, AutomatedEmail>{

		private AutomatedEmailFielder(){}

		@Override
		public Class<AutomatedEmailKey> getKeyFielderClass() {
			return AutomatedEmailKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(AutomatedEmail d){
			return FieldTool.createList(
				new StringField(F.subject, d.subject, MySqlColumnType.MAX_LENGTH_VARCHAR),
				new StringField(F.content, d.content, MySqlColumnType.INT_LENGTH_LONGTEXT));
		}

	}

	/** construct *************************************************************/

	private AutomatedEmail(){
		this(null, null);
	}

	public AutomatedEmail(String suject, String content){
		this.key = new AutomatedEmailKey();
		this.subject = suject;
		this.content = content;
	}

	/** databean **************************************************************/

	@Override
	public Class<AutomatedEmailKey> getKeyClass() {
		return AutomatedEmailKey.class;
	}

	@Override
	public AutomatedEmailKey getKey() {
		return key;
	}

	/** get/set ***************************************************************/

	public String getSubject(){
		return subject;
	}

	public String getContent(){
		return content;
	}

}

