package com.hotpads.spark.data.downloaders;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.spark.config.properties.AwsConfig;
import com.hotpads.spark.config.properties.SparkJobDirectoryConfig;
import com.hotpads.spark.data.compressors.DataCompressor;
import com.hotpads.spark.data.compressors.SnappyDataCompressor;
import com.hotpads.spark.data.jobs.DataPipeline;
import com.hotpads.spark.data.jobs.DataPipelineJob;
import com.hotpads.spark.data.uploaders.DataUploader;
import com.hotpads.spark.data.uploaders.S3DataUploader;
import com.hotpads.spark.data.uploaders.S3MetaDataProvider;
import com.hotpads.util.core.java.ReflectionTool;
import com.hotpads.util.core.profile.PhaseTimer;

public class MysqlTableExistenceCheckerAndDownloader<PK extends PrimaryKey<PK>,D extends Databean<PK, D>>
implements TableExistenceCheckerAndDownloader<PK, D> {
	private static final Logger logger = LoggerFactory.getLogger(MysqlTableExistenceCheckerAndDownloader.class);

	private static final String S3_BUCKET_NAME = "rdt-emr";
	private static final String FILE_PATH_SEPERATOR = File.separator;
	private static final String OUTPUT_DIR_PATH = "/mnt/input";
	private static final String EMPTY_STRING = "";

	private final AwsConfig awsConfig = new AwsConfig();

	private final List<DataPipeline> dataPipelines = new ArrayList<>();
	private final String jobId;
	private final Map<Class<? extends Databean<PK, D>>, SparkTableParameters<PK, D>> databeanClassToParameters;
	private final SparkJobDirectoryConfig sparkDirConfig;

	public MysqlTableExistenceCheckerAndDownloader(String jobId,
			Map<Class<? extends Databean<PK, D>>, SparkTableParameters<PK, D>> databeanClassToParameters,
			SparkJobDirectoryConfig sparkDirConfig){
		this.jobId = jobId;
		this.databeanClassToParameters = databeanClassToParameters;
		this.sparkDirConfig = sparkDirConfig;
	}

	@Override
	public Map<Class<? extends Databean<PK, D>>, SparkTableParameters<PK, D>> downloadTablesIfNecessary()
			throws ExecutionException, InterruptedException, AmazonServiceException {
		PhaseTimer timer = new PhaseTimer("uploadInputFiles");
		for(SparkTableParameters<PK, D> downloadOptions : databeanClassToParameters.values()) {
			if(downloadOptions.getS3InputPath() == null) {
				downloadTableAndAddToDataPipeline(downloadOptions, sparkDirConfig, dataPipelines);
			}
		}
		executePipelines();
		logger.info(timer.add("done").toString());
		return databeanClassToParameters;
	}

	private String getDownloadedTableVersionId(String inputTable, int hours, SparkJobDirectoryConfig sparkDirConfig)
			throws AmazonServiceException {
		S3MetaDataProvider s3InformationProvider = new S3MetaDataProvider();
		return s3InformationProvider.getLatestDownloadWithinTimeFrame(S3_BUCKET_NAME,
				sparkDirConfig.getInputFolderPath() + inputTable + FILE_PATH_SEPERATOR, hours);
	}

	private DataPipeline downloadTable(SparkTableParameters<PK, D> downloadOptions,
			SparkJobDirectoryConfig sparkDirConfig) {
		DataCompressor dataCompressor = new SnappyDataCompressor();
		String s3UploadLocation = sparkDirConfig.getS3TableLocationToUpload(downloadOptions.getTableName());
		downloadOptions.setS3InputPath(sparkDirConfig.getS3InputTablePath(s3UploadLocation));
		DataDownloader tableDownloader = new DbDataDownloader<PK, D>(jobId, downloadOptions.getTableName(),
				downloadOptions.getColumnNameCsv(), ReflectionTool.create(downloadOptions.getFielderClass()),
				downloadOptions.getSortedStorageReader(), OUTPUT_DIR_PATH);
		AWSCredentials credentials = new BasicAWSCredentials(awsConfig.getEmrAccessKey(), awsConfig.getEmrSecretKey());
		DataUploader dataUploader = new S3DataUploader(S3_BUCKET_NAME, s3UploadLocation, credentials);
		return new DataPipeline(tableDownloader, dataCompressor, dataUploader);
	}

	private boolean isTableAlreadyDownloaded(String tableName, String downloadedTableVersionId) {
		if(downloadedTableVersionId == null || EMPTY_STRING.equals(downloadedTableVersionId)) {
			return false;
		}
		logger.info(tableName + " table has already been downloaded within specified hours. Using version Id: {}",
				downloadedTableVersionId);
		return true;
	}

	private void downloadTableAndAddToDataPipeline(SparkTableParameters<PK, D> downloadOptions,
			SparkJobDirectoryConfig sparkDirConfig, List<DataPipeline> dataPipelines) {
		String downloadedTableVersionId = getDownloadedTableVersionId(downloadOptions.getTableName(),
				downloadOptions.getHoursToRedownload(), sparkDirConfig);
		boolean tableExisting = isTableAlreadyDownloaded(downloadOptions.getTableName(), downloadedTableVersionId);
		if(!tableExisting) {
			dataPipelines.add(downloadTable(downloadOptions, sparkDirConfig));
		} else {
			downloadOptions.setS3InputPath(sparkDirConfig.getS3InputTablePath(downloadOptions.getTableName(),
					downloadedTableVersionId));
		}

	}

	private void executePipelines() throws ExecutionException, InterruptedException {
		if(!dataPipelines.isEmpty()) {
			DataPipelineJob job = new DataPipelineJob(dataPipelines);
			job.executePipelines();
		}
	}
}
