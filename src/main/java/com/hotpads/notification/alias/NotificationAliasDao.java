package com.hotpads.notification.alias;

import com.hotpads.notification.databean.NotificationLog;

public interface NotificationAliasDao{

	NotificationAlias[] getAllAliases();

	Iterable<Subscriber> getSubscribers(NotificationAlias alias);

	Iterable<Moderator> getModerators(NotificationAlias alias);

	AutomatedEmail[] getAutomatedEmail(NotificationAlias alias);

	Iterable<NotificationLog> getLogs(NotificationAlias alias, int limit);

}
