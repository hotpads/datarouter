package com.hotpads.datarouter.routing.comparator;

import java.util.Comparator;

import com.hotpads.datarouter.routing.DataRouter;

public class ComparatorDataRouter implements Comparator<DataRouter>{

	@Override
	public int compare(DataRouter o1, DataRouter o2){
		return o1.getName().compareToIgnoreCase(o2.getName());
	}

}
