package com.hotpads.conveyor;

public interface Conveyor extends Comparable<Conveyor>, Runnable{

	String getName();
	boolean shouldRun();


	@Override
	default int compareTo(Conveyor other){
		return getName().compareTo(other.getName());
	}

}
