package com.hotpads.notification.destination;

import java.util.List;

import com.hotpads.notification.databean.NotificationUserType;

public interface NotificationDestinationService{

	List<NotificationDestination> getActiveDestinations(NotificationUserType userType, String userToken);

	void registerDestinatination(NotificationDestination destination);

	void deactiveDestination(NotificationDestinationKey destinationKey);

	void activeDestination(NotificationDestinationKey destinationKey);

}
