package com.hotpads.util.core.iterable.scanner.batch;

import java.util.concurrent.Callable;

import com.hotpads.util.core.iterable.scanner.Scanner;

public interface BatchLoader<T>
extends Callable<BatchLoader<T>>, Scanner<T>{
	
	boolean isLastBatch();
	BatchLoader<T> getNextLoader();
	
}
