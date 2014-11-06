package com.hotpads.notification.alias;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.hotpads.notification.alias.databean.AutomatedEmail;
import com.hotpads.notification.alias.databean.Moderator;
import com.hotpads.notification.alias.databean.Subscriber;
import com.hotpads.notification.databean.NotificationLog;

public interface NotificationAliasService{

	List<NotificationAlias> getAllAliases();

	Iterable<Subscriber> getSubscribers(NotificationAlias alias);

	Iterable<Moderator> getModerators(NotificationAlias alias);

	List<AutomatedEmailType> getAutomatedEmail(NotificationAlias alias);

	List<NotificationLog> getLogs(NotificationAlias alias, int limit);

	Map<NotificationLog,AutomatedEmail> getEmailForLogs(Collection<NotificationLog> logs);

	void addModeratorIfAuthorized(HttpServletRequest request, NotificationAlias alias, String moderatorEmail);

	void removeModeratorIfAuthorized(HttpServletRequest request, NotificationAlias alias, String moderatorEmail);

	String getUserEmail(HttpServletRequest request);

	void subscribeIfAuthorized(HttpServletRequest request, NotificationAlias alias, String email);

	void unsubscribeIfAuthorized(HttpServletRequest request, NotificationAlias alias, String email);

	boolean requestHasAuthorityOnList(HttpServletRequest request, NotificationAlias alias);

}
