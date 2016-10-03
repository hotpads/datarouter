package com.hotpads.datarouter.conveyor;

public interface Conveyor extends Comparable<Conveyor>, Runnable{

	String getName();
	void shutdown();


	@Override
	default int compareTo(Conveyor other){
		return getName().compareTo(other.getName());
	}

}
