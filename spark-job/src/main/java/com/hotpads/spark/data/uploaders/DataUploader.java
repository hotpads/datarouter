package com.hotpads.spark.data.uploaders;


public interface DataUploader{
	/**
	 * The S3 URI points to a folder path on S3 that contains all partial files of the data.
	 */
	String getUploadFolderUri();

	void upload(String pathToFile) throws InterruptedException;
}
