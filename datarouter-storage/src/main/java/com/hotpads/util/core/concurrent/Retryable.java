package com.hotpads.util.core.concurrent;

import java.util.concurrent.Callable;

/**
 * Marker interface indicating that the call() can be retried without problems.
 */
public interface Retryable<T> extends Callable<T>{

}
