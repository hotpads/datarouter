package com.hotpads.util.core.concurrent;

import java.util.concurrent.Semaphore;

public class MutableSemaphore extends Semaphore{

	public MutableSemaphore(int permits, boolean fairness) {
		super(permits, fairness);
	}

	public MutableSemaphore(){
		super(0);
	}

	@Override
	public void reducePermits(int reduction){
		super.reducePermits(reduction);
	}
}
