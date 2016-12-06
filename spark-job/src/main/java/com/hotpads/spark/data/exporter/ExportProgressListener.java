package com.hotpads.spark.data.exporter;

public interface ExportProgressListener<T>{
	void update(T args);
}
