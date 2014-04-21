package com.hotpads.notification.timing;

public interface NotificationTimingStrategy {
	/**
	 * NOT USED
	 * should the first message be rate-limited?
	 * @return
	 */
	boolean shouldDelayFirstMessage();

	/**
	 * usually something like 60 seconds
	 * @return
	 */
	int getMinDelaySeconds();


	/**
	 * fixed delay between notification
	 */
	int getStandardDelay();
	
	/**
	 * NOT USED
	 * exponential growth rate.  something like 2, 3, or 4
	 * @return
	 */
	int getDelayGrowthRate();

	/**
	 * NOT USED
	 * saved search may be 1 hour, feeds 1 day, etc
	 * @return
	 */
	int getMaxDelaySeconds();
}
