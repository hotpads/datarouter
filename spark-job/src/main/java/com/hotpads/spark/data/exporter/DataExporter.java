package com.hotpads.spark.data.exporter;

import java.util.concurrent.Callable;

//supports multi-part downloads
public interface DataExporter extends Callable<Void>{
	//during multi-part downloads, after successful download of each part, the listeners will be updated
	void addProgressListener(ExportProgressListener<String> listener);
}
