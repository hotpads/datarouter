package com.hotpads.datarouter.client.imp.s3;

import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.HttpConstants;
import org.apache.hadoop.fs.s3.S3Exception;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.apache.tools.ant.taskdefs.optional.j2ee.HotDeploymentTool;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;

public class S3GetTool{
	private static Logger logger = Logger.getLogger(S3GetTool.class);

	public static void getFile(String bucketName, String key, File file) throws IOException{
		try{
			AmazonS3 s3 = S3Tool.getS3();
			GetObjectRequest get = new GetObjectRequest(bucketName, key);
			s3.getObject(get, file);
		}catch(Exception e){
			throw new S3Exception(e);
		}
	}
	
	public static ObjectMetadata getFileMetadata(AmazonS3 s3, String bucketName, String key) {
		if (s3 == null) {
			s3 = S3Tool.getS3();
		}
		try {
			return s3.getObjectMetadata(bucketName, key);
		} catch (AmazonS3Exception as3e) {
			if (as3e.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
				return null;
			}
			throw as3e;
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}
	
	public static boolean existFile(AmazonS3 s3, String bucketName, String key) {
		return getFileMetadata(s3, bucketName, key) != null;
	}

//	public static void main(String[] args) throws IOException{
//		String localFileFolder = "/mnt/hdd/zillowData/regionData1/";
//		String localFileName = "RegionGeometryModified.out";
//		String BUCKET_files = "files.hotpads.com";
//		String remoteFileFolder = "zillow/areas/regionData/";
//		File localFile = new File(localFileFolder + localFileName);
//		
//		getFile(BUCKET_files, remoteFileFolder + localFileName, localFile);
//	}
	
}
