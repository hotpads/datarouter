package com.hotpads.notification.alias;

import javax.servlet.http.HttpServletRequest;

import com.hotpads.notification.databean.NotificationLog;

public interface NotificationAliasDao{

	NotificationAlias[] getAllAliases();

	Iterable<Subscriber> getSubscribers(NotificationAlias alias);

	Iterable<Moderator> getModerators(NotificationAlias alias);

	AutomatedEmail[] getAutomatedEmail(NotificationAlias alias);

	Iterable<NotificationLog> getLogs(NotificationAlias alias, int limit);

	void addModeratorIfAuthorized(HttpServletRequest request, NotificationAlias alias, String moderatorEmail);

	void removeModeratorIfAuthorized(HttpServletRequest request, NotificationAlias alias, String moderatorEmail);

	String getUserEmail(HttpServletRequest request);

	void subscribeIfAuthorized(HttpServletRequest request, NotificationAlias alias, String email);

	void unsubscribeIfAuthorized(HttpServletRequest request, NotificationAlias alias, String email);

	boolean requestHaveAuthorityOnList(HttpServletRequest request, NotificationAlias alias);

}
