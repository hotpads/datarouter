package com.hotpads.spark.data;

public interface ProgressListener<T>{
	void update(T args);
}
