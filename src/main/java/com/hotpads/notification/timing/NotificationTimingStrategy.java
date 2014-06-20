package com.hotpads.notification.timing;

public interface NotificationTimingStrategy {

	//TODO Same as getMinDelaySeconds = 0 ? so we can remove it.
	/**
	 * NOT USED <br>
	 * Should the first message be rate-limited?
	 */
	boolean shouldDelayFirstMessage();

	/**
	 * Minimum delay between the requesting and the sending of the Notification (in seconds)
	 */
	int getMinDelaySeconds();


	/**
	 * Minimum delay between two Notification sending (in seconds)
	 */
	int getStandardDelaySeconds();
	
	/**
	 * NOT USED <br>
	 * exponential growth rate.  something like 2, 3, or 4
	 */
	int getDelayGrowthRate();

	/**
	 * NOT USED <br>
	 * saved search may be 1 hour, feeds 1 day, etc
	 */
	int getMaxDelaySeconds();

}
