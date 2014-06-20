package com.hotpads.datarouter.client.imp.s3;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.fs.s3.S3Exception;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;

public class S3GetTool{

	public static void getFile(String bucketName, String key, File file) throws IOException{
		try{
			AmazonS3 s3 = S3Tool.getS3();
			GetObjectRequest get = new GetObjectRequest(bucketName, key);
			s3.getObject(get, file);
		}catch(Exception e){
			throw new S3Exception(e);
		}
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
