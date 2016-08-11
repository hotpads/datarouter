package com.hotpads.job.record;

import java.util.Comparator;

import com.hotpads.datarouter.util.core.DrComparableTool;

public class LongRunningTaskStartTimeComparator implements Comparator<LongRunningTask>{

	private final boolean ascending;

	public LongRunningTaskStartTimeComparator(boolean ascending){
		this.ascending = ascending;
	}

	@Override
	public int compare(LongRunningTask left, LongRunningTask right) {
		int compare = DrComparableTool.nullFirstCompareTo(left.getStartTime(), right.getStartTime());
		return ascending ? compare : -1 * compare;
	}

}
