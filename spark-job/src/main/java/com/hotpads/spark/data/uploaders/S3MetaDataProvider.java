package com.hotpads.spark.data.uploaders;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.hotpads.spark.config.properties.AwsConfig;

public class S3MetaDataProvider{
	private static final Logger logger = LoggerFactory.getLogger(S3MetaDataProvider.class);
	private static final String EMPTY_STRING = "";
	private static AwsConfig awsConfig = new AwsConfig();
	private static AWSCredentials credentials = new BasicAWSCredentials(awsConfig.getEmrAccessKey(), awsConfig
			.getEmrSecretKey());
	private static AmazonS3 conn = new AmazonS3Client(credentials);

	public String getLatestDownloadWithinTimeFrame(String bucketName, String prefix, int hours)
			throws AmazonClientException,AmazonServiceException{
		if(bucketName == null || bucketName.isEmpty() || prefix == null || prefix.isEmpty()) {
			return null;
		}
		List<String> versions = getDownloadedVersions(bucketName, prefix);
		if(versions == null || versions.isEmpty()) {
			return null;
		}
		versions.sort(versionComparator);
		String latestversion = StringUtils.split(versions.get(0), File.separator)[3];
		if(isLatestVersionWithinTimeFrame(latestversion, hours)) {
			return latestversion;
		}
		return EMPTY_STRING;
	}

	private boolean isLatestVersionWithinTimeFrame(String latestValue, int timeframehours){
		long hours = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - Long.valueOf(latestValue));
		if(hours < timeframehours) {
			return true;
		}
		return false;
	}

	private List<String> getDownloadedVersions(String bucketName, String prefix) throws AmazonClientException,
			AmazonServiceException{
		String delimiter = "/";
		if(!prefix.endsWith(delimiter)) {
			prefix += delimiter;
		}
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix(prefix)
				.withDelimiter(delimiter);
		ObjectListing objects = conn.listObjects(listObjectsRequest);
		return objects.getCommonPrefixes();

	}

	public Comparator<String> versionComparator = new Comparator<String>(){

		@Override
		public int compare(String str1, String str2){
			// get just the version part
			String v1 = StringUtils.split(str1, File.separator)[3];
			// get just the version part
			String v2 = StringUtils.split(str2, File.separator)[3];
			if(Long.valueOf(v1) > (Long.valueOf(v2))) {
				return -1;
			}else if(Long.valueOf(v1) < ((Long.valueOf(v2)))) {
				return 1;
			}
			return 0;

		}

	};
}
