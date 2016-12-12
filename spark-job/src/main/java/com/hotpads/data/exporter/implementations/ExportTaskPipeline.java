package com.hotpads.data.exporter.implementations;

import com.hotpads.data.compressors.DataCompressor;
import com.hotpads.data.exporter.DataExporter;
import com.hotpads.data.uploaders.DataUploader;

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
