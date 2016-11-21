package com.hotpads.spark.data.jobs;

import com.hotpads.spark.data.compressors.DataCompressor;
import com.hotpads.spark.data.downloaders.DataDownloader;
import com.hotpads.spark.data.uploaders.DataUploader;

public class DataPipeline{
	private final DataDownloader dataDownloader;
	private final DataCompressor dataCompressor;
	private final DataUploader dataUploader;

	public DataPipeline(DataDownloader dataDownloader, DataCompressor dataCompressor,
			DataUploader dataUploader){
		this.dataDownloader = dataDownloader;
		this.dataCompressor = dataCompressor;
		this.dataUploader = dataUploader;
	}

	public DataDownloader getDataDownloader(){
		return dataDownloader;
	}

	public DataCompressor getDataCompressor(){
		return dataCompressor;
	}

	public DataUploader getDataUploader(){
		return dataUploader;
	}
}
