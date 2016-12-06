package com.hotpads.spark.data.uploaders;


public interface DataUploader{
	String getUploadLocation();

	void upload(String pathToFile) throws InterruptedException;
}
