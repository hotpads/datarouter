package com.hotpads.notification.timing;

public interface NotificationTimingStrategy {

	/**
	 * @return The minimum age that should have a request to be process by the notification service
	 */
	int getMinSendableAgeSeconds();
	
	//TODO Same as getMinDelaySeconds = 0 ? so we can remove it.
	/**
	 * NOT USED <br>
	 * Should the first message be rate-limited?
	 */
	boolean shouldDelayFirstMessage();

	/**
	 * A group where the last request is older than this age will be sent
	 * 
	 * If no new notification Request have been received during this delay the group is sent
	 */
	int getMinDelaySeconds();//TODO should be renamed


	/**
	 * Minimum delay between two same type Notification sending (in seconds)
	 */
	int getStandardDelaySeconds();
	
	/**
	 * NOT USED <br>
	 * exponential growth rate.  something like 2, 3, or 4
	 */
	int getDelayGrowthRate();

	/**
	 * A group where the first request is older than this age will be sent
	 * 
	 * Any request older than this delays will definitely trigger the sent for its group
	 */
	int getMaxDelaySeconds();

	/**
	 * @return the max number of request in a notification (when the number of request reach this number the
	 *         notification is triggered)
	 */
	int getMaxItems();
	
	/**
	 * Minimum delay between two same type and same channel Notification sending (in seconds)
	 */
	int getDelayForChannel();

	/**
	 * @return The age after which a request can be droped id not sent.
	 */
	int getDropableAgeSeconds();
}
