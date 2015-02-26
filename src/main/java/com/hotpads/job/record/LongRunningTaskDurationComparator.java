package com.hotpads.job.record;

import java.util.Comparator;

import com.hotpads.datarouter.util.core.DrComparableTool;

public class LongRunningTaskDurationComparator implements Comparator<LongRunningTask>{

	private boolean ascending = false;
	
	public LongRunningTaskDurationComparator(boolean ascending){
		this.ascending = ascending;
	}
	
	@Override
	public int compare(LongRunningTask a, LongRunningTask b) {
		int compare = DrComparableTool.nullFirstCompareTo(a.getStartTime(), b.getStartTime());
		if(ascending){
			return compare;
		}
		return -1 * compare;
	}
	
}
