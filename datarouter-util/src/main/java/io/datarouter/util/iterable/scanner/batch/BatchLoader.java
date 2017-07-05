package io.datarouter.util.iterable.scanner.batch;

import java.util.concurrent.Callable;

import io.datarouter.util.iterable.scanner.Scanner;

public interface BatchLoader<T>
extends Callable<BatchLoader<T>>, Scanner<T>{

	boolean isLastBatch();
	BatchLoader<T> getNextLoader();

}
