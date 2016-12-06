package com.hotpads.spark.data.exporter;

public interface S3ExportPathResolver{
	String getS3BucketName();

	String getS3Url(String inputTable, String tableVersionId);

	String getS3Url(String s3UploadLocation);

	String getVersionedTablePath(String inputTable, String tableVersionId);
}
