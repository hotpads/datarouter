package com.hotpads.notification;

import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.type.index.UniqueIndexNode;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.notification.databean.NotificationItemLog;
import com.hotpads.notification.databean.NotificationItemLogKey;
import com.hotpads.notification.databean.NotificationLog;
import com.hotpads.notification.databean.NotificationLogKey;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.databean.NotificationRequestKey;
import com.hotpads.notification.destination.NotificationDestination;
import com.hotpads.notification.destination.NotificationDestinationByAppDeviceId;
import com.hotpads.notification.destination.NotificationDestinationByAppDeviceIdKey;
import com.hotpads.notification.destination.NotificationDestinationKey;
import com.hotpads.notification.log.NotificationLogById;
import com.hotpads.notification.log.NotificationLogByIdKey;
import com.hotpads.notification.preference.NotificationPreference;
import com.hotpads.notification.preference.NotificationPreferenceKey;
import com.hotpads.notification.timing.NotificationTimingStrategy;
import com.hotpads.notification.timing.NotificationTimingStrategyKey;
import com.hotpads.notification.timing.NotificationTimingStrategyMapping;
import com.hotpads.notification.timing.NotificationTimingStrategyMappingKey;
import com.hotpads.notification.tracking.NotificationTrackingEvent;
import com.hotpads.notification.tracking.NotificationTrackingEventKey;

public interface NotificationNodes extends Router{
	public SortedMapStorageNode<NotificationRequestKey,NotificationRequest> getNotificationRequest();

	public SortedMapStorageNode<NotificationLogKey,NotificationLog> getNotificationLog();
	public UniqueIndexNode<NotificationLogKey,NotificationLog,NotificationLogByIdKey,NotificationLogById>
			getNotificationLogById();

	public SortedMapStorageNode<NotificationItemLogKey,NotificationItemLog> getNotificationItemLog();

	public IndexedSortedMapStorageNode<NotificationDestinationKey,NotificationDestination> getNotificationDestination();
	public UniqueIndexNode<NotificationDestinationKey,NotificationDestination, NotificationDestinationByAppDeviceIdKey,
			NotificationDestinationByAppDeviceId> getNotificationDestinationByAppDeviceId();

	public SortedMapStorage<NotificationPreferenceKey,NotificationPreference> getNotificationPreference();

	public IndexedSortedMapStorageNode<NotificationTrackingEventKey, NotificationTrackingEvent>
			getNotificationTrackingEvent();

	public SortedMapStorageNode<NotificationTimingStrategyKey,NotificationTimingStrategy> getNotificationTimingStrategy();
	public SortedMapStorageNode<NotificationTimingStrategyMappingKey,NotificationTimingStrategyMapping>
		getNotificationTimingStrategyMapping();

}
