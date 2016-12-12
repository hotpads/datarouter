package com.hotpads.data.exporter;

/**
 * Resolver for getting the final S3 path destination the exported file will be uploaded to. This path is not a
 * fixed value and so we cannout store it in {@code Exportprameters}. Implementers of the exporting service (for
 * particular type of client, say our spark app) will need to provide a resolver that the client agrees to.
 */
public interface S3ExportPathResolver{
	String getS3BucketName();

	String getS3Url(String inputTable, String tableVersionId);

	String getS3Url(String s3UploadLocation);

	String getVersionedTablePath(String inputTable, String tableVersionId);
}
