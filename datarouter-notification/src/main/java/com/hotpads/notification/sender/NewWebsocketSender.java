//package com.hotpads.notification.sender;
//
//import javax.inject.Inject;
//
//import com.google.gson.Gson;
//import com.hotpads.notification.databean.NotificationUserId;
//import com.hotpads.notification.destination.NotificationDestination;
//import com.hotpads.notification.result.NotificationSendingResult;
//import com.hotpads.notification.sender.template.BaseBuiltTemplate;
//import com.hotpads.notification.sender.template.BuiltWebsocketTemplate;
//import com.hotpads.websocket.session.PushService;
//
//public class NewWebSocketSender extends NotificationSender{
//
//	@Inject
//	private PushService pushService;
//	@Inject
//	private Gson gson;
//
//	private BuiltWebsocketTemplate template;
//
////	@Override
////	public void setTemplate(NotificationTemplate template){
////		this.template = (WebSocketNotificationTemplate)template;
////	}
//
//	@Override
//	public boolean send(BaseBuiltTemplate builtTemplate, NotificationDestination notificationDestination,
//			NotificationUserId notificationUserId,NotificationSendingResult result){
//		template = (BuiltWebsocketTemplate)builtTemplate;
//		pushService.forward(template.getUserToken(), template.getSessionId(), gson.toJson(
//				new WebSocketNotificationMessage(template.getMessageType(), template.getNotificationId(), template
//				.getPayload())));
//		return true;
//	}
//
//}