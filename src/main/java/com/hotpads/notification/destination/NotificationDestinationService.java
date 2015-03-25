package com.hotpads.notification.destination;

import java.util.Collection;
import java.util.List;

public interface NotificationDestinationService{

	public List<NotificationDestination> getActiveDestinations(String userToken,
			Collection<NotificationDestinationApp> apps);

	void registerDestinatination(NotificationDestination destination);

	void deactiveDestination(NotificationDestinationKey destinationKey);

	void activeDestination(NotificationDestinationKey destinationKey);

}
