package com.hotpads.datarouter.storage.databean.update;

import org.testng.Assert;
import org.testng.annotations.Test;

public class CaseEnforcingDatabeanUpdateTests{

	@Test
	public void testReplaceInsteadOfMerge(){
		CaseEnforcingDatabeanUpdateTestBean oldBean = new CaseEnforcingDatabeanUpdateTestBean("a", "b", 1, "d", "f1",
				2L, "f3", 4d);
		CaseEnforcingDatabeanUpdateTestBean newBean = new CaseEnforcingDatabeanUpdateTestBean("a", "B", 1, "d", "f1",
				2L, "f3", 4d);

		CaseEnforcingDatabeanUpdate<CaseEnforcingDatabeanUpdateTestBeanKey,CaseEnforcingDatabeanUpdateTestBean> update
				= new CaseEnforcingDatabeanUpdate<>(null);
		Assert.assertTrue(update.replaceInsteadOfMerge(oldBean, newBean));
		newBean = new CaseEnforcingDatabeanUpdateTestBean("a", "b", 1, "d", "f1", 2L, "f3", 4d);
		Assert.assertFalse(update.replaceInsteadOfMerge(oldBean, newBean));
		newBean = new CaseEnforcingDatabeanUpdateTestBean("A", "b", 1, "d", "f1", 2L, "f3", 4d);
		Assert.assertTrue(update.replaceInsteadOfMerge(oldBean, newBean));
		oldBean = new CaseEnforcingDatabeanUpdateTestBean("A", "B", 1, "d", "f1", 2L, "f3", 4d);
		Assert.assertTrue(update.replaceInsteadOfMerge(oldBean, newBean));
		oldBean = new CaseEnforcingDatabeanUpdateTestBean("A", "b", 1, "d", "f1", 2L, "f3", 4d);
		Assert.assertFalse(update.replaceInsteadOfMerge(oldBean, newBean));
	}

}