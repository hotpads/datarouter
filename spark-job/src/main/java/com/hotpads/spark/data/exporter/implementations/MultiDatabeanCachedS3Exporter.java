package com.hotpads.spark.data.exporter.implementations;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
import com.hotpads.spark.data.compressors.DataCompressor;
import com.hotpads.spark.data.compressors.SnappyDataCompressor;
import com.hotpads.spark.data.exporter.DataExporter;
import com.hotpads.spark.data.exporter.ExportParameters;
import com.hotpads.spark.data.exporter.MultiDatabeanExporter;
import com.hotpads.spark.data.exporter.S3ExportPathResolver;
import com.hotpads.spark.data.uploaders.DataUploader;
import com.hotpads.spark.data.uploaders.S3DataUploader;
import com.hotpads.spark.data.uploaders.S3MetaDataProvider;
import com.hotpads.util.core.java.ReflectionTool;
import com.hotpads.util.core.profile.PhaseTimer;

public class MultiDatabeanCachedS3Exporter<PK extends PrimaryKey<PK>,D extends Databean<PK, D>>
implements MultiDatabeanExporter<PK, D>{
	private static final Logger logger = LoggerFactory.getLogger(MultiDatabeanCachedS3Exporter.class);

	private static final String OUTPUT_DIR_PATH = "/mnt/input";
	private static final String EMPTY_STRING = "";

	private final String awsAccessKey;
	private final String awsSecretKey;
	private final List<ExportTaskPipeline> exportTaskPipelines = new ArrayList<>();
	private final String jobId;
	private final Map<Class<? extends Databean<PK, D>>, ExportParameters<PK, D>> databeanClassToParameters;
	private final S3ExportPathResolver s3ExportPathResolver;

	public MultiDatabeanCachedS3Exporter(String jobId,
			Map<Class<? extends Databean<PK, D>>, ExportParameters<PK, D>> databeanClassToParameters,
			S3ExportPathResolver s3ExportPathResolver, String awsAccessKey, String awsSecretKey){
		this.jobId = jobId;
		this.databeanClassToParameters = databeanClassToParameters;
		this.s3ExportPathResolver = s3ExportPathResolver;
		this.awsAccessKey = awsAccessKey;
		this.awsSecretKey = awsSecretKey;
	}

	@Override
	public Map<Class<? extends Databean<PK, D>>, String> export()
			throws ExecutionException, InterruptedException, AmazonServiceException{
		PhaseTimer timer = new PhaseTimer("uploadInputFiles");
		Map<Class<? extends Databean<PK, D>>, String> results = new LinkedHashMap<>();
		for(ExportParameters<PK, D> parameters : databeanClassToParameters.values()){
			String destination = downloadTableAndAddToDataPipeline(parameters, exportTaskPipelines);
			results.put(parameters.getDatabeanClass(), destination);
		}
		executePipelines();
		logger.info(timer.add("done").toString());
		return results;
	}

	private String getDownloadedTableVersionId(String inputTable, int hours) throws AmazonServiceException{
		S3MetaDataProvider s3InformationProvider = new S3MetaDataProvider(awsAccessKey, awsSecretKey);
		return s3InformationProvider.getLatestExportWithinTimeFrame(s3ExportPathResolver.getS3BucketName(),
				s3ExportPathResolver.getVersionedTablePath(inputTable, null), hours);
	}

	private ExportTaskPipeline downloadTable(ExportParameters<PK, D> parameters){
		DataCompressor dataCompressor = new SnappyDataCompressor();
		String s3UploadLocation = s3ExportPathResolver.getVersionedTablePath(parameters.getTableName(),
				Long.toString(System.currentTimeMillis()));
		DataExporter tableDownloader = new DatabeanExporter<>(jobId, parameters.getTableName(),
				parameters.getColumnNameCsv(), ReflectionTool.create(
				parameters.getFielderClass()),
				parameters.getSortedStorageReader(), OUTPUT_DIR_PATH);
		AWSCredentials credentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
		DataUploader dataUploader = new S3DataUploader(s3ExportPathResolver.getS3BucketName(), s3UploadLocation,
				credentials);
		return new ExportTaskPipeline(tableDownloader, dataCompressor, dataUploader);
	}

	private boolean isTableAlreadyDownloaded(String tableName, String downloadedTableVersionId){
		if(downloadedTableVersionId == null || EMPTY_STRING.equals(downloadedTableVersionId)){
			return false;
		}
		logger.info(tableName + " table has already been downloaded within specified hours. Using version Id: {}",
				downloadedTableVersionId);
		return true;
	}

	private String downloadTableAndAddToDataPipeline(ExportParameters<PK, D> downloadOptions,
			List<ExportTaskPipeline> exportTaskPipelines){
		String downloadedTableVersionId = getDownloadedTableVersionId(
				downloadOptions.getTableName(),
				downloadOptions.getHoursToRedownload());
		boolean tableExisting = isTableAlreadyDownloaded(downloadOptions.getTableName(),
				downloadedTableVersionId);
		if(!tableExisting){
			ExportTaskPipeline pipeline = downloadTable(downloadOptions);
			exportTaskPipelines.add(pipeline);
			return s3ExportPathResolver.getS3Url(pipeline.getDataUploader().getUploadLocation());
		}else{
			return s3ExportPathResolver.getS3Url(downloadOptions.getTableName(),
					downloadedTableVersionId);
		}
	}

	private void executePipelines() throws ExecutionException, InterruptedException{
		if(!exportTaskPipelines.isEmpty()){
			ExportPipelineParallelExecutor job = new ExportPipelineParallelExecutor(exportTaskPipelines);
			job.executePipelines();
		}
	}
}
