package com.hotpads.notification.alias;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.sf.json.JSONObject;

import com.google.gson.Gson;
import com.hotpads.datarouter.util.core.IterableTool;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.dispatcher.DatarouterDispatcher;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.JsonMav;
import com.hotpads.handler.util.RequestTool;
import com.hotpads.notification.alias.databean.AutomatedEmail;
import com.hotpads.notification.alias.databean.Moderator;
import com.hotpads.notification.alias.databean.Subscriber;
import com.hotpads.notification.databean.NotificationLog;

@Singleton
public class NotificationAliasHandler extends BaseHandler{

	private static final String
			COMMAND_REGEX = ".*" + DatarouterDispatcher.NOTIFICATION_ALIAS + "/",
			JSP = "/jsp/admin/datarouter/notification/alias.jsp";

	@Inject
	private NotificationAliasService notificationAliasDao;
	@Inject
	private Gson gson;

	public Mav getRedirectMav(NotificationAlias alias){
		String aliasUrl = "";
		if(alias != null){
			aliasUrl = "/" + alias.getPersistentName();
		}
		return new Mav(Mav.REDIRECT + servletContext.getContextPath() + DatarouterDispatcher.URL_DATAROUTER
				+ DatarouterDispatcher.NOTIFICATION_ALIAS + aliasUrl);
	}

	@Override
	protected Mav handleDefault() throws Exception{
		Mav actionResult = handleAction();
		if(actionResult != null){
			return actionResult;
		}
		Mav mav = new Mav(JSP);
		NotificationAlias selectedAlias = getSelectedAlias();
		if(selectedAlias != null){
			if(RequestTool.isAjax(request)){
				return details(selectedAlias);
			}
			mav.put("preLoadedAlias", selectedAlias.getPersistentName());
		}
		List<NotificationAlias> aliases = notificationAliasDao.getAllAliases();
		mav.put("aliases", aliases);
		mav.put("userEmail", notificationAliasDao.getUserEmail(request));
		return mav;
	}

	private NotificationAlias getSelectedAlias(){
		if(request.getPathInfo().matches(COMMAND_REGEX + ".+")){
			String aliasName;
			try{
				aliasName = URLDecoder.decode(request.getPathInfo().replaceAll(COMMAND_REGEX, ""), "UTF-8");
			}catch(UnsupportedEncodingException e){
				throw new RuntimeException(e);
			}
			return new NotificationAlias(aliasName);
		}
		return null;
	}

	private Mav handleAction(){
		NotificationAlias selectedAlias = getSelectedAlias();
		String addModeratorEmail = params.optional("addModerator", null);
		if(addModeratorEmail != null){
			notificationAliasDao.addModeratorIfAuthorized(request, selectedAlias, addModeratorEmail);
			return getRedirectMav(selectedAlias);
		}
		String removeModeratorEmail = params.optional("removeModerator", null);
		if(removeModeratorEmail != null) {
			notificationAliasDao.removeModeratorIfAuthorized(request, selectedAlias, removeModeratorEmail);
			return getRedirectMav(selectedAlias);
		}
		String subscribeEmail = params.optional("subscribeEmail", null);
		if(subscribeEmail != null) {
			notificationAliasDao.subscribeIfAuthorized(request, selectedAlias, subscribeEmail);
			return getRedirectMav(selectedAlias);
		}
		String unsubscribeEmail = params.optional("unsubscribeEmail", null);
		if(unsubscribeEmail != null) {
			notificationAliasDao.unsubscribeIfAuthorized(request, selectedAlias, unsubscribeEmail);
			return getRedirectMav(selectedAlias);
		}
		return null;
	}

	private Mav details(NotificationAlias alias){
		JSONObject jsonObject = new JSONObject();

		jsonObject.put("alias", alias);

		Iterable<Subscriber> subscribers = notificationAliasDao.getSubscribers(alias);
		jsonObject.put("subscribers", gson.toJson(IterableTool.asList(subscribers)));

		Iterable<Moderator> moderators = notificationAliasDao.getModerators(alias);
		jsonObject.put("moderators", gson.toJson(IterableTool.asList(moderators)));
		
		List<AutomatedEmailType> automatedEmails = notificationAliasDao.getAutomatedEmail(alias);
		jsonObject.put("automatedEmails", gson.toJson(automatedEmails));

		List<NotificationLog> logs = notificationAliasDao.getLogs(alias, 100);
		jsonObject.put("notificationLogs", gson.toJson(logs));

		Map<NotificationLog,AutomatedEmail> emailForLogs = notificationAliasDao.getEmailForLogs(logs);
		AutomatedEmail[] emails = new AutomatedEmail[logs.size()];
		for(int i = 0; i < logs.size(); i++){
			emails[i] = emailForLogs.get(logs.get(i));
		}
		jsonObject.put("emails", gson.toJson(emails));

		boolean hasAuthorityOnList = notificationAliasDao.requestHasAuthorityOnList(request, alias);
		jsonObject.put("hasAuthorityOnList", hasAuthorityOnList);
		
		return new JsonMav(jsonObject);
	}

}
