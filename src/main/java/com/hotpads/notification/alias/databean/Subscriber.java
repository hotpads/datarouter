package com.hotpads.notification.alias.databean;

import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseLatin1Fielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.notification.alias.NotificationAlias;


/** CREATE SCRIPT
com.hotpads.notification.databean.alias.Subscriber{
  PK{
    StringEnumField<Alias> alias,
    StringField email
  }
  DateField subscriptionDate,
  StringField subscriptionAuthor

}

*/
public class Subscriber extends BaseDatabean<SubscriberKey,Subscriber> {

	/** fields ****************************************************************/

	private SubscriberKey key;

	private Date subscriptionDate;
	private String subscriptionAuthor;


	/** columns ***************************************************************/

	public static class F {
		public static final String
			subscriptionDate = "subscriptionDate",
			subscriptionAuthor = "subscriptionAuthor";
	}

	/** fielder ***************************************************************/

	public static class SubscriberFielder extends BaseLatin1Fielder<SubscriberKey,Subscriber>{

		private SubscriberFielder(){}

		@Override
		public Class<SubscriberKey> getKeyFielderClass() {
			return SubscriberKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(Subscriber d){
			return FieldTool.createList(
				new DateField(F.subscriptionDate, d.subscriptionDate),
				new StringField(F.subscriptionAuthor, d.subscriptionAuthor, MySqlColumnType.MAX_LENGTH_VARCHAR));
		}		
	}

	/** construct *************************************************************/

	private Subscriber(){
		this(new NotificationAlias(null), null, null);
	}

	public Subscriber(NotificationAlias alias, String email, String authorEmail){
		this.key = new SubscriberKey(alias, email);
		this.subscriptionDate = new Date();
		this.subscriptionAuthor = authorEmail;
	}

	/** databean **************************************************************/

	@Override
	public Class<SubscriberKey> getKeyClass() {
		return SubscriberKey.class;
	}

	@Override
	public SubscriberKey getKey() {
		return key;
	}

	/** get/set ***************************************************************/

	public String getEmail(){
		return key.getEmail();
	}

	public NotificationAlias getAlias(){
		return key.getAlias();
	}

}

