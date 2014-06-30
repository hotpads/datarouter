package com.hotpads.job.record;

import java.util.Comparator;

import com.hotpads.util.core.ComparableTool;

public class LongRunningTaskDurationComparator implements Comparator<LongRunningTask>{

	private boolean ascending = false;
	
	public LongRunningTaskDurationComparator(boolean ascending){
		this.ascending = ascending;
	}
	
	@Override
	public int compare(LongRunningTask a, LongRunningTask b) {
		int compare = ComparableTool.nullFirstCompareTo(a.getStartTime(), b.getStartTime());
		if(ascending){
			return compare;
		}
		return -1 * compare;
	}
	
}
