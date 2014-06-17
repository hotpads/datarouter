package com.hotpads.datarouter.client.imp.s3;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.fs.s3.S3Exception;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.hotpads.datarouter.backup.imp.s3.BackupRegionToS3;

public class S3PutTool{

	public static void putFile(boolean createBucket, String bucketName, File file, String key,
			CannedAccessControlList acl, String contentType, String cacheControl) throws IOException{
		try{
			AmazonS3 s3 = S3Tool.getS3();
			PutObjectRequest put = new PutObjectRequest(bucketName, key, file);
			put.setCannedAcl(acl);
			ObjectMetadata meta = new ObjectMetadata();
			meta.setCacheControl(cacheControl);
			meta.setContentType(contentType);
			put.setMetadata(meta);
			s3.putObject(put);
		}catch(Exception e){
			throw new S3Exception(e);
		}
	}
	
	public static void main(String[] args) throws IOException{
		String localFileFolder = "/mnt/hdd/zillowData/regionData/";
		String localFileName = "RegionGeometryModified.out";
		String BUCKET_files = "files.hotpads.com";
		String remoteFileFolder = "zillow/areas/regionData";
		File localFile = new File(localFileFolder + localFileName);
		putFile(false, BUCKET_files, localFile,
			remoteFileFolder + localFileName,
			CannedAccessControlList.Private,
			S3Headers.ContentType.GZIP.getMimeType(), 
			S3Headers.CACHE_CONTROL_NO_CACHE);
	}
	
}
