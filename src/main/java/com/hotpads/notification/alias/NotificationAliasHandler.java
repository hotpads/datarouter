package com.hotpads.notification.alias;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.sf.json.JSONObject;

import com.google.gson.Gson;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.JsonMav;
import com.hotpads.handler.util.RequestTool;
import com.hotpads.notification.databean.NotificationLog;
import com.hotpads.util.core.IterableTool;

@Singleton
public class NotificationAliasHandler extends BaseHandler{

	private static final String NOTIFICATION_ALIAS = ".*/notification/alias/";

	@Inject
	private NotificationAliasDao notificationAliasDao;
	@Inject
	private Gson gson;

	@Override
	protected Mav handleDefault() throws Exception{
		Mav mav = new Mav("/notification/alias");
		if (request.getPathInfo().matches(NOTIFICATION_ALIAS + ".+")) {
			String selectedAlias = request.getPathInfo().replaceAll(NOTIFICATION_ALIAS, "");
			if(RequestTool.isAjax(request)){
				return details(selectedAlias);
			}
			mav.put("preLoadedAlias", selectedAlias);
		}
		NotificationAlias[] aliases = notificationAliasDao.getAllAliases();
		mav.put("aliases", aliases);
		return mav;
	}

	private Mav details(String cmd){
		JSONObject jsonObject = new JSONObject();

		NotificationAlias alias = new NotificationAlias(cmd);
		jsonObject.put("alias", alias);

		Iterable<Subscriber> subscribers = notificationAliasDao.getSubscribers(alias);
		jsonObject.put("subscribers", gson.toJson(IterableTool.asList(subscribers)));

		Iterable<Moderator> moderators = notificationAliasDao.getModerators(alias);
		jsonObject.put("moderators", gson.toJson(IterableTool.asList(moderators)));
		
		AutomatedEmail[] automatedEmails = notificationAliasDao.getAutomatedEmail(alias);
		jsonObject.put("automatedEmails", gson.toJson(automatedEmails));

		Iterable<NotificationLog> notificationLogs = notificationAliasDao.getLogs(alias, 100);
		jsonObject.put("notificationLogs", gson.toJson(IterableTool.asList(notificationLogs)));

		return new JsonMav(jsonObject);
	}

}
