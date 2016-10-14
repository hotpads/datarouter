package com.hotpads.conveyor;

public interface Conveyor extends Comparable<Conveyor>, Runnable{

	String getName();


	@Override
	default int compareTo(Conveyor other){
		return getName().compareTo(other.getName());
	}

}
