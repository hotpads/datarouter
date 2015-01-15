package com.hotpads.datarouter.routing.comparator;

import java.util.Comparator;

import com.hotpads.datarouter.routing.Datarouter;

public class DatarouterComparator implements Comparator<Datarouter>{

	@Override
	public int compare(Datarouter o1, Datarouter o2){
		return o1.getName().compareToIgnoreCase(o2.getName());
	}

}
