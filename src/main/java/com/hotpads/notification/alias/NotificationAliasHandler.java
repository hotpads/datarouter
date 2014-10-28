package com.hotpads.notification.alias;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.MessageMav;

@Singleton
public class NotificationAliasHandler extends BaseHandler{

	@Inject
	private NotificationAliasDao notificationAliasDao;
	
	@Override
	protected Mav handleDefault() throws Exception{
		Iterable<NotificationAlias> aliases = notificationAliasDao.getAllAliases();
		Iterable<Subscriber> allSubscribers = notificationAliasDao.getAllSubscribers();
//		for(Subscriber subscriber : allSubscribers){
//			
//		}
		return new MessageMav(aliases + "" + allSubscribers);
	}

}
