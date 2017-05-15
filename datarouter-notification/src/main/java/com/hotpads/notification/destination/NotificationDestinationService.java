package com.hotpads.notification.destination;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.notification.NotificationNodes;
import com.hotpads.util.core.collections.Range;

@Singleton
public class NotificationDestinationService{
	private static final Logger logger = LoggerFactory.getLogger(NotificationDestinationService.class);

	@Inject
	private NotificationNodes notificationNodes;

	public void deactivateDestination(NotificationDestinationByAppDeviceIdKey destinationByAppDeviceIdKey){
		Range<NotificationDestinationByAppDeviceIdKey> prefix = new Range<>(destinationByAppDeviceIdKey, true,
				destinationByAppDeviceIdKey, true);
		Iterable<NotificationDestinationByAppDeviceId> destinationByAppDeviceIds = notificationNodes
				.getNotificationDestinationByAppDeviceId().scan(prefix, null);
		List<NotificationDestinationKey> destinationKeys = NotificationDestinationByAppDeviceId.getDestinationKeys(
				destinationByAppDeviceIds);
		deactivateDestination(destinationKeys);
	}

	private void deactivateDestination(List<NotificationDestinationKey> destinationKeys){
		List<NotificationDestination> destinations = new ArrayList<>();
		Iterator<NotificationDestinationKey> destinationKeyiterator = destinationKeys.iterator();
		while(destinationKeyiterator.hasNext()){
			NotificationDestinationKey notificationDestinationKey = destinationKeyiterator.next();
			if(notificationDestinationKey.getApp() == null){
				notificationNodes.getNotificationDestination().streamWithPrefix(notificationDestinationKey, null)
						.filter(destination -> destination.getKey().getDeviceId()
								== notificationDestinationKey.getDeviceId())
						.forEach(destinations::add);
				destinationKeyiterator.remove();
				logger.warn("deactivating destination with partial key {}", notificationDestinationKey);
			}
		}
		destinations.addAll(notificationNodes.getNotificationDestination().getMulti(
				destinationKeys, null));
		destinations.forEach(d -> d.setActive(false));
		notificationNodes.getNotificationDestination().putMulti(destinations, null);
	}

	public void deactivateDestinationByTokenAndDevice(NotificationDestinationKey destinationKey){
		if(DrStringTool.isNullOrEmptyOrWhitespace(destinationKey.getToken())){
			return;
		}
		deactivateDestination(notificationNodes.getNotificationDestination()
				.streamKeysWithPrefix(new NotificationDestinationKey(destinationKey.getToken(), null, null), null)
				.filter(key -> DrStringTool.nullSafe(destinationKey.getDeviceId()).equals(DrStringTool.nullSafe(key
						.getDeviceId())))
				.collect(Collectors.toList()));
	}

	public void updateDestination(NotificationDestination destination){
		Objects.requireNonNull(destination.getKey().getToken());
		Objects.requireNonNull(destination.getKey().getDeviceId());

		if(destination.getActive()){
			NotificationDestinationAppName app = Objects.requireNonNull(destination.getKey().getApp());
			destination.setCreated(new Date());
			String deviceId = destination.getKey().getDeviceId();
			deactivateDestination(new NotificationDestinationByAppDeviceIdKey(app, deviceId));
			notificationNodes.getNotificationDestination().put(destination, null);
		}else{
			deactivateDestination(Arrays.asList(destination.getKey()));
		}
	}
}
