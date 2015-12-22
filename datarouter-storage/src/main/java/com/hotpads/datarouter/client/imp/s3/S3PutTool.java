package com.hotpads.datarouter.client.imp.s3;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferProgress;
import com.amazonaws.services.s3.transfer.Upload;
import com.hotpads.datarouter.util.core.DrNumberFormatter;
import com.hotpads.util.core.concurrent.ThreadTool;

public class S3PutTool{
	private static final Logger logger = LoggerFactory.getLogger(S3PutTool.class);

	//can only upload upto 5GB for a single file.
	public static void putFile(boolean createBucket, String bucketName, File file, String key,
			CannedAccessControlList acl, String contentType, String cacheControl){
		AmazonS3 s3 = S3Tool.getS3();
		PutObjectRequest put = new PutObjectRequest(bucketName, key, file);
		put.setCannedAcl(acl);
		ObjectMetadata meta = new ObjectMetadata();
		meta.setCacheControl(cacheControl);
		meta.setContentType(contentType);
		put.setMetadata(meta);
		s3.putObject(put);
	}

	public static void uploadLargeFile(File localFile, String existingBucketName, String keyName){
		AmazonS3 s3 = S3Tool.getS3();
		TransferManager transferManager = new TransferManager(s3);
		try{
			final Upload myUpload = transferManager.upload(existingBucketName, keyName, localFile);
			while( ! myUpload.isDone()){
				TransferProgress progress = myUpload.getProgress();
				logger.warn(progress.getPercentTransferred() + "%, a total of "
						+ DrNumberFormatter.addCommas(progress.getBytesTransferred())
						+ " Bytes transfered.");
				ThreadTool.sleep(2000);
			}
		}catch(AmazonClientException amazonClientException){
			logger.error("Upload was aborted. Unable to upload file: {}", keyName, amazonClientException);
		}
		transferManager.shutdownNow();
	}

//	public static void main(String[] args) throws IOException{
//		String localFileFolder = "/mnt/hdd/zillowData/regionData/";
//		String localFileName = "RegionGeometryModified.out";
//		String BUCKET_files = "files.hotpads.com";
//		String remoteFileFolder = "zillow/areas/regionData/";
//		File localFile = new File(localFileFolder + localFileName);
//		putFile(false, BUCKET_files, localFile,
//			remoteFileFolder + localFileName,
//			CannedAccessControlList.Private,
//			S3Headers.ContentType.GZIP.getMimeType(),
//			S3Headers.CACHE_CONTROL_NO_CACHE);
//	}

}
