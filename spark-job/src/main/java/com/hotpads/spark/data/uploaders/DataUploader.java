package com.hotpads.spark.data.uploaders;


public interface DataUploader{
	void upload(String pathToFile) throws InterruptedException;
}
