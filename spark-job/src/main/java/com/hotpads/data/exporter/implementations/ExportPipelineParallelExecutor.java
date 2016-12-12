package com.hotpads.data.exporter.implementations;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import com.hotpads.data.compressors.DataCompressor;
import com.hotpads.data.exporter.DataExporter;

class ExportPipelineParallelExecutor{
	private static final int EXECUTOR_THREADS = 5;
	private static final long TIME_BEFORE_UPLOAD_CHECK_MS = 1000;

	private final List<ExportTaskPipeline> exportTaskPipelines;
	private final ExecutorService dataDownloaderExecutorService;
	private final ExecutorService dataCompressorExecutorService;
	private final ExecutorService dataUploaderExecutorService;

	public ExportPipelineParallelExecutor(List<ExportTaskPipeline> exportTaskPipelines){
		this.exportTaskPipelines = exportTaskPipelines;
		this.dataDownloaderExecutorService = Executors.newFixedThreadPool(EXECUTOR_THREADS);
		this.dataCompressorExecutorService = Executors.newFixedThreadPool(EXECUTOR_THREADS);
		this.dataUploaderExecutorService = Executors.newFixedThreadPool(EXECUTOR_THREADS);
	}

	public void executePipelines() throws ExecutionException, InterruptedException{
		try{
			doExecutePipelines();
		}finally{
			dataDownloaderExecutorService.shutdown();
			dataCompressorExecutorService.shutdown();
			dataUploaderExecutorService.shutdown();
		}
	}

	private void doExecutePipelines() throws ExecutionException, InterruptedException{
		List<Future> dataDownloaderFutures = new LinkedList<>();
		CompletionService<Callable<Void>> dataCompressorExecutorCompService = new ExecutorCompletionService<>(
				dataCompressorExecutorService);
		AtomicInteger numCompressors = new AtomicInteger(0);
		for(ExportTaskPipeline exportTaskPipeline : exportTaskPipelines){
			DataExporter dataExporter = exportTaskPipeline.getDataExporter();
			dataExporter.addProgressListener(fileDownloadedPath -> {
				dataCompressorExecutorCompService.submit(getCompressor(exportTaskPipeline, fileDownloadedPath));
				numCompressors.getAndIncrement();
			});

			dataDownloaderFutures.add(dataDownloaderExecutorService.submit(dataExporter));
		}

		CompletionService<Void> dataUploaderExecutorCompService = new ExecutorCompletionService<>(
				dataUploaderExecutorService);

		int download = 0;
		while(dataDownloaderFutures.size() > 0){
			try{
				Future dataDownloaderFuture = dataDownloaderFutures.get(download++);
				dataDownloaderFuture.get(TIME_BEFORE_UPLOAD_CHECK_MS, TimeUnit.MILLISECONDS);
				dataDownloaderFutures.remove(dataDownloaderFuture);
			}catch(TimeoutException e){
				// ignored
			}
			while(numCompressors.get() > 0){
				uploadDataAfterCompressing(dataCompressorExecutorCompService, dataUploaderExecutorCompService);
				numCompressors.getAndDecrement();
			}
			if(download >= dataDownloaderFutures.size()){
				download = 0;
			}
		}
	}

	private Callable<Callable<Void>> getCompressor(ExportTaskPipeline exportTaskPipeline, String fileDownloadedPath){
		return () -> {
			DataCompressor dataCompressor = exportTaskPipeline.getDataCompressor();
			String compressedFilePath = fileDownloadedPath + dataCompressor.getFileExtension();
			exportTaskPipeline.getDataCompressor().compress(fileDownloadedPath, compressedFilePath);
			return getUploader(exportTaskPipeline, compressedFilePath);
		};
	}

	private Callable<Void> getUploader(ExportTaskPipeline exportTaskPipeline, String compressedFilePath){
		return () -> {
			exportTaskPipeline.getDataUploader().upload(compressedFilePath);
			return null;
		};
	}

	private void uploadDataAfterCompressing(CompletionService<Callable<Void>> dataCompressorExecutorCompService,
			CompletionService<Void> dataUploaderExecutorCompService) throws ExecutionException, InterruptedException{
		dataUploaderExecutorCompService.submit(dataCompressorExecutorCompService.take().get());
		dataUploaderExecutorCompService.take().get();
	}

}
