package com.hotpads.notification;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.google.common.base.Preconditions;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.databean.NotificationUserId;
import com.hotpads.notification.databean.NotificationUserType;
import com.hotpads.notification.result.NotificationSendingResult;

public class NotificationTesterHandler extends BaseHandler{

	private static final String
			P_NOTIFICATION_REQUEST_DATA = "notificationRequestData",
			P_USER_ID = "userId",
			P_USER_TYPE = "userType",
			P_NOTIFICATION_TYPE = "notificationType",
			P_NOTIFICATION_CHANNEL = "channel",
			P_NOTIFICATION_ASYNCH = "async",
			JSP_NOTIFICATION_TESTER = "/jsp/admin/datarouter/notification/tester.jsp";

	@Inject
	private NotificationManager notificationManager;

	@Override
	protected Mav handleDefault(){
 		Mav mav = new Mav(JSP_NOTIFICATION_TESTER);
 		if(params.optional(P_NOTIFICATION_TYPE).orElse(null) != null){
 			Optional<List<NotificationSendingResult>> results = sendNotification();
 			if(results.isPresent()){
				String failures = results.get().stream()
						.filter(result -> result.getFailureReason() != null)
						.map(result -> result.getTemplate().getName() + ": " + result.getFailureReason())
						.collect(Collectors.joining(", "));
				mav.put("failures", failures);
				mav.put("numAttempted", results.get().size());
				mav.put("numSent", NotificationSendingResult.countSuccesses(results.get()));
 			}else{
 				mav.put("requested", true);
 			}
 		}
 		mav.put("userTypes", NotificationUserType.values());
 		return mav;
	}

	private Optional<List<NotificationSendingResult>> sendNotification(){
		NotificationUserType userType = NotificationUserType.valueOf(params.required(P_USER_TYPE));
		String id = params.required(P_USER_ID);
		NotificationUserId userId = new NotificationUserId(userType, id);

		String type = params.required(P_NOTIFICATION_TYPE);
		String channel = params.optional(P_NOTIFICATION_CHANNEL).orElse(null);
		Boolean async = params.requiredBoolean(P_NOTIFICATION_ASYNCH);

		String[] dataArr = Preconditions.checkNotNull(request.getParameterValues(P_NOTIFICATION_REQUEST_DATA));
		List<NotificationRequest> notificationRequests = Stream.of(dataArr)
				.map(data -> new NotificationRequest(userId, type, data, channel)).collect(Collectors.toList());

		if(async){
			if (notificationRequests.size() > 1){
				notificationManager.request(notificationRequests);
			} else{
				notificationManager.request(notificationRequests.get(0));
			}
			return Optional.empty();
		} else {
			return Optional.of(notificationRequests.size() > 1 ? notificationManager.send(notificationRequests) :
				notificationManager.send(notificationRequests.get(0)));
		}
	}

}
