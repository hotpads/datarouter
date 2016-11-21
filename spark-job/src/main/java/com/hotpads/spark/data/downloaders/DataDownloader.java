package com.hotpads.spark.data.downloaders;

import java.util.concurrent.Callable;

import com.hotpads.spark.data.ProgressListener;

//supports multi-part downloads
public interface DataDownloader extends Callable<Void>{
	//during multi-part downloads, after successful download of each part, the listeners will be updated
	void addProgressListener(ProgressListener<String> listener);
}
