package com.hotpads.data.exporter;

public interface ExportProgressListener<T>{
	void update(T args);
}
