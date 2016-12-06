package com.hotpads.spark.data.uploaders;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerConfiguration;
import com.amazonaws.services.s3.transfer.Upload;

public class S3DataUploader implements DataUploader{
	private static final Logger logger = LoggerFactory.getLogger(S3DataUploader.class);

	private static final int NUM_TRANSFER_THREADS = 5;

	private static final long DEFAULT_HDFS_BLOCK_SIZE = 128 * 1024 * 1024;

	private final String bucketName;
	private final String uploadLocation;
	private AWSCredentials credentials;

	public S3DataUploader(String bucketName, String uploadLocation,AWSCredentials credentials){
		this.bucketName = bucketName;
		this.uploadLocation = uploadLocation;
		this.credentials = credentials;
	}

	@Override
	public void upload(String pathToFile) throws InterruptedException{
		logger.info("About to upload file {}. S3 bucket: {}, upload location: {}", pathToFile, bucketName,
				uploadLocation);
		File outputFile = new File(pathToFile);
		TransferManager transferManager = new TransferManager(new AmazonS3Client(credentials),
				Executors.newFixedThreadPool(NUM_TRANSFER_THREADS));
		TransferManagerConfiguration transferManagerConfiguration = new TransferManagerConfiguration();
		transferManagerConfiguration.setMultipartUploadThreshold(2 * DEFAULT_HDFS_BLOCK_SIZE);
		transferManagerConfiguration.setMinimumUploadPartSize(DEFAULT_HDFS_BLOCK_SIZE);

		transferManager.setConfiguration(transferManagerConfiguration);
		// TransferManager processes all transfers asynchronously,
		// so this call will return immediately.
		Upload upload = transferManager.upload(bucketName + File.separator + uploadLocation,
				outputFile.getName(), outputFile);

		ProgressListener progressListener =
				progressEvent -> logger.info("Transferred bytes: " + progressEvent.getBytesTransferred());
		upload.addProgressListener(progressListener);

		try{
			upload.waitForCompletion();
			logger.info("Upload of file {} is complete.", pathToFile);
		}catch(AmazonClientException amazonClientException){
			logger.error("Unable to upload file, upload was aborted.", amazonClientException);
		}
	}

	/**************************** getter *******************************/

	public String getUploadLocation(){
		return uploadLocation;
	}

	public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException{
		AWSCredentials credentials = new BasicAWSCredentials(args[3], args[4]);
		S3DataUploader uploadDataToS3Job = new S3DataUploader(args[0], args[1],credentials);
		uploadDataToS3Job.upload(args[2]);
		System.exit(0);
	}
}
