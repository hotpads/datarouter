package com.hotpads.job.trigger;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TriggerInfoTests{

	@Test
	public void testJobIsRunning(){
		TriggerInfo triggerInfo = new TriggerInfo();
		Assert.assertFalse(triggerInfo.isRunning());
		// successful
		Assert.assertTrue(triggerInfo.switchToRunning());
		Assert.assertTrue(triggerInfo.isRunning());
		// not successful
		Assert.assertFalse(triggerInfo.switchToRunning());
		triggerInfo.setRunning(false);
		Assert.assertFalse(triggerInfo.isRunning());
	}

}
