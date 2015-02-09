package com.hotpads.notification.alias.databean;

import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCharacterSet;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.notification.alias.NotificationAlias;


/** CREATE SCRIPT
com.hotpads.notification.databean.alias.Moderator{
  PK{
    StringEnumField<Alias> alias,
    StringField email
  }
  DateField membershipDate,
  StringField membershipAuthor

}

*/
public class Moderator extends BaseDatabean<ModeratorKey,Moderator> {

	/** fields ****************************************************************/

	private ModeratorKey key;

	private Date membershipDate;
	private String membershipAuthor;


	/** columns ***************************************************************/

	public static class F {
		public static final String
			membershipDate = "membershipDate",
			membershipAuthor = "membershipAuthor";
	}

	/** fielder ***************************************************************/

	public static class ModeratorFielder
		extends BaseDatabeanFielder<ModeratorKey, Moderator>{
		public ModeratorFielder(){
		}

		@Override
		public Class<ModeratorKey> getKeyFielderClass() {
			return ModeratorKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(Moderator d){
			return FieldTool.createList(
				new DateField(F.membershipDate, d.membershipDate),
				new StringField(F.membershipAuthor, d.membershipAuthor, MySqlColumnType.MAX_LENGTH_VARCHAR));
		}
		
		@Override
		public MySqlCharacterSet getCharacterSet(Moderator databean){
			return MySqlCharacterSet.latin1;			
		}
		
		@Override
		public MySqlCollation getCollation(Moderator databean){
			return MySqlCollation.latin1_swedish_ci;
		}		

	}

	/** construct *************************************************************/

	private Moderator(){
		this(new NotificationAlias(null), null, null);
	}

	public Moderator(NotificationAlias alias, String moderatorEmail, String authorEmail){
		this.key = new ModeratorKey(alias, moderatorEmail);
		this.membershipDate = new Date();
		this.membershipAuthor = authorEmail;
	}

	/** databean **************************************************************/

	@Override
	public Class<ModeratorKey> getKeyClass() {
		return ModeratorKey.class;
	}

	@Override
	public ModeratorKey getKey() {
		return key;
	}

	/** get/set ***************************************************************/

	public void setKey(ModeratorKey key) {
		this.key = key;
	}

	public Date getMembershipDate(){
		return membershipDate;
	}

	public void setMembershipDate(Date membershipDate){
		this.membershipDate = membershipDate;
	}

	public String getMembershipAuthor(){
		return membershipAuthor;
	}

	public void setMembershipAuthor(String membershipAuthor){
		this.membershipAuthor = membershipAuthor;
	}

}

