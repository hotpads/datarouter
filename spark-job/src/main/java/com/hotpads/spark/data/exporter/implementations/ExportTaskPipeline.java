package com.hotpads.spark.data.exporter.implementations;

import com.hotpads.spark.data.compressors.DataCompressor;
import com.hotpads.spark.data.exporter.DataExporter;
import com.hotpads.spark.data.uploaders.DataUploader;

class ExportTaskPipeline{
	private final DataExporter dataExporter;
	private final DataCompressor dataCompressor;
	private final DataUploader dataUploader;

	public ExportTaskPipeline(DataExporter dataExporter, DataCompressor dataCompressor,
			DataUploader dataUploader){
		this.dataExporter = dataExporter;
		this.dataCompressor = dataCompressor;
		this.dataUploader = dataUploader;
	}

	public DataExporter getDataExporter(){
		return dataExporter;
	}

	public DataCompressor getDataCompressor(){
		return dataCompressor;
	}

	public DataUploader getDataUploader(){
		return dataUploader;
	}
}
