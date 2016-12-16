package com.hotpads.notification;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.notification.timing.NotificationTimingStrategy;
import com.hotpads.notification.timing.NotificationTimingStrategyKey;
import com.hotpads.notification.timing.NotificationTimingStrategyMapping;
import com.hotpads.notification.timing.NotificationTimingStrategyMappingKey;
import com.hotpads.util.core.Duration;

public class NotificationTimingStrategyHandler extends BaseHandler{

	private static final String
			P_MAPPINGS = "mappings",
			P_TIMINGS = "timings",
			P_DURATION_PATTERN = "pattern",
			//fields that can be empty but are required (used for NotificationTimingStrategyMapping)
			P_CHANNEL_PREFIX = "channelPrefix",
			P_TIMING_STRATEGY = "timingStrategy",
			JSP_NOTIFICATION_TIMING = "/jsp/admin/datarouter/notification/timing.jsp";

	@Inject
	private NotificationNodes notificationNodes;

	@Handler(defaultHandler = true)
	protected Mav showPage(){
		Mav mav = new Mav(JSP_NOTIFICATION_TIMING);

		List<NotificationTimingStrategyMapping> mappings = notificationNodes.getNotificationTimingStrategyMapping()
				.stream(null, null).collect(Collectors.toList());
		mav.put(P_MAPPINGS, mappings);
		List<NotificationTimingStrategy> timings = notificationNodes.getNotificationTimingStrategy()
				.stream(null, null).collect(Collectors.toList());
		mav.put(P_TIMINGS, timings);
		mav.put(P_DURATION_PATTERN, Duration.REGEX);

		return mav;
	}

	@Handler
	protected void addOrUpdateMapping(String type){
		NotificationTimingStrategyMapping mapping = new NotificationTimingStrategyMapping(type,
				params.required(P_CHANNEL_PREFIX), params.required(P_TIMING_STRATEGY));
		notificationNodes.getNotificationTimingStrategyMapping().put(mapping, null);
	}

	@Handler
	protected void deleteMapping(String type){
		NotificationTimingStrategyMappingKey mapping = new NotificationTimingStrategyMappingKey(type,
				params.required(P_CHANNEL_PREFIX));
		notificationNodes.getNotificationTimingStrategyMapping().delete(mapping, null);
	}

	@Handler
	protected void addOrUpdateTiming(String name, String minSendableAge, Long maxItems, String droppableAge,
			String delayForChannel, String minDelay, String standardDelay, String maxDelay){
		NotificationTimingStrategy timing = new NotificationTimingStrategy(name, minSendableAge, maxItems, droppableAge,
				delayForChannel, minDelay, standardDelay, maxDelay);
		notificationNodes.getNotificationTimingStrategy().put(timing, null);
	}

	@Handler
	protected void deleteTiming(String name){
		NotificationTimingStrategyKey timing = new NotificationTimingStrategyKey(name);
		notificationNodes.getNotificationTimingStrategy().delete(timing, null);
	}
}
