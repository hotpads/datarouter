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
			//NotificationTimingStrategyMapping
			P_TYPE = "type",
			P_CHANNEL_PREFIX = "channelPrefix",
			P_TIMING_STRATEGY = "timingStrategy",
			//NotificationTimingStrategy
			P_NAME = "name",
			P_MIN_SENDABLE_AGE = "minSendableAge",
			P_MAX_ITEMS = "maxItems",
			P_DROPPABLE_AGE = "droppableAge",
			P_DELAY_FOR_CHANNEL = "delayForChannel",
			P_MIN_DELAY = "minDelay",
			P_STANDARD_DELAY = "standardDelay",
			P_MAX_DELAY = "maxDelay",

			JSP_NOTIFICATION_TIMING = "/jsp/admin/datarouter/notification/timing.jsp";

	@Inject
	private NotificationNodes notificationNodes;

	@Handler(defaultHandler=true)
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

	@Handler()
	protected void addOrUpdateMapping(){
		NotificationTimingStrategyMapping mapping = new NotificationTimingStrategyMapping(params.required(P_TYPE),
				params.required(P_CHANNEL_PREFIX),
				params.required(P_TIMING_STRATEGY));
		notificationNodes.getNotificationTimingStrategyMapping().put(mapping, null);
	}

	@Handler()
	protected void deleteMapping(){
		NotificationTimingStrategyMappingKey mapping = new NotificationTimingStrategyMappingKey(params.required(P_TYPE),
				params.required(P_CHANNEL_PREFIX));
		notificationNodes.getNotificationTimingStrategyMapping().delete(mapping, null);
	}

	@Handler()
	protected void addOrUpdateTiming(){
		NotificationTimingStrategy timing = new NotificationTimingStrategy(params.required(P_NAME),
				params.required(P_MIN_SENDABLE_AGE),
				params.requiredLong(P_MAX_ITEMS),
				params.required(P_DROPPABLE_AGE),
				params.required(P_DELAY_FOR_CHANNEL),
				params.required(P_MIN_DELAY),
				params.required(P_STANDARD_DELAY),
				params.required(P_MAX_DELAY));
		notificationNodes.getNotificationTimingStrategy().put(timing, null);
	}

	@Handler()
	protected void deleteTiming(){
		NotificationTimingStrategyKey timing = new NotificationTimingStrategyKey(params.required(P_NAME));
		notificationNodes.getNotificationTimingStrategy().delete(timing, null);
	}
}
