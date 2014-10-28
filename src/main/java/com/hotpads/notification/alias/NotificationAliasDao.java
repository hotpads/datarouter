package com.hotpads.notification.alias;

public interface NotificationAliasDao{

	Iterable<Subscriber> getAllSubscribers();

	Iterable<NotificationAlias> getAllAliases();

}
