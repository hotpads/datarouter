package com.hotpads.notification.alias.databean;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.DateFieldKey;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.notification.alias.NotificationAlias;

public class Subscriber extends BaseDatabean<SubscriberKey,Subscriber>{

	/** fields ****************************************************************/

	private SubscriberKey key;

	private Date subscriptionDate;
	private String subscriptionAuthor;


	/** columns ***************************************************************/

	public static class FieldKeys{
		public static final DateFieldKey subscriptionDate = new DateFieldKey("subscriptionDate");
		public static final StringFieldKey subscriptionAuthor = new StringFieldKey("subscriptionAuthor");
	}

	/** fielder ***************************************************************/

	public static class SubscriberFielder extends BaseDatabeanFielder<SubscriberKey,Subscriber>{

		private SubscriberFielder(){
			super(SubscriberKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(Subscriber subscriber){
			return Arrays.asList(
				new DateField(FieldKeys.subscriptionDate, subscriber.subscriptionDate),
				new StringField(FieldKeys.subscriptionAuthor, subscriber.subscriptionAuthor));
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
	public Class<SubscriberKey> getKeyClass(){
		return SubscriberKey.class;
	}

	@Override
	public SubscriberKey getKey(){
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

