package com.hotpads.config.job.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.hotpads.config.job.dao.hibernate.DeleteJoblet;
import com.hotpads.config.job.dao.hibernate.GetJobletForProcessing;
import com.hotpads.config.job.dao.hibernate.GetJobletStatuses;
import com.hotpads.config.job.dao.hibernate.UpdateJobletAndQueue;
import com.hotpads.config.job.databean.Joblet;
import com.hotpads.config.job.databean.JobletData;
import com.hotpads.config.job.databean.JobletDataKey;
import com.hotpads.config.job.databean.JobletKey;
import com.hotpads.config.job.dto.JobletSummary;
import com.hotpads.config.job.enums.JobletStatus;
import com.hotpads.config.job.enums.JobletType;
import com.hotpads.dao.datarouter.DRH;
import com.hotpads.databean.search.feed.Feed;
import com.hotpads.databean.search.listing.image.BaseSearchImage;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.handler.exception.ExceptionRecord;
import com.hotpads.handler.exception.ExceptionRecorder;
import com.hotpads.job.JobRouter;
import com.hotpads.job.joblet.JobletProcess;
import com.hotpads.job.joblet.type.FeedImportJoblet;
import com.hotpads.job.joblet.type.ImageCachingJoblet;
import com.hotpads.job.trigger.JobExceptionCategory;
import com.hotpads.search.SearchRouter;
import com.hotpads.search.data.dao.ListingStatusDao;
import com.hotpads.search.data.dao.SearchImageDao;
import com.hotpads.search.data.dao.hibernate.listing.UpdateSearchImageStatus;
import com.hotpads.util.core.BatchTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.profile.PhaseTimer;
import com.hotpads.util.image.CachedImageStatus;

@Singleton
@Component
public class JobletService{
	private static Logger logger = LoggerFactory.getLogger(JobletService.class);

	public static final int MAX_JOBLET_RETRIES = 10;

	private final Datarouter datarouter;
	private final JdbcFieldCodecFactory jdbcFieldCodecFactory;
	private static JobRouter JOB_ROUTER;
	private final SearchRouter searchRouter;
	private final ExceptionRecorder exceptionRecorder;
	private final SearchImageDao searchImageDao;
	private final ListingStatusDao listingStatusDao;

	@Inject
	public JobletService(Datarouter datarouter, JdbcFieldCodecFactory jdbcFieldCodecFactory,
			JobRouter jobRouter, SearchRouter searchRouter, ExceptionRecorder exceptionRecorder,
			SearchImageDao searchImageDao, ListingStatusDao listingStatusDao){
		this.datarouter = datarouter;
		this.jdbcFieldCodecFactory = jdbcFieldCodecFactory;
		JOB_ROUTER = jobRouter;
		this.searchRouter = searchRouter;
		this.exceptionRecorder = exceptionRecorder;
		this.searchImageDao = searchImageDao;
		this.listingStatusDao = listingStatusDao;
	}

	@Deprecated//transition hack to work in job and site project
	public static JobRouter getJobRouter(){
		return JOB_ROUTER != null ? JOB_ROUTER : DRH.job();
	}

	public Joblet getJobletForProcessing(JobletType type, String reservedBy, long jobletTimeoutMs, boolean rateLimited){
		return datarouter.run(new GetJobletForProcessing(jobletTimeoutMs, MAX_JOBLET_RETRIES,
				reservedBy, type, datarouter, getJobRouter(), rateLimited));
	}

	public JobletData getJobletData(Joblet joblet){
		Long jobletDataId = joblet.getJobletDataId();
		JobletData jobletData = getJobletData(jobletDataId);
		return jobletData;
	}

	public static JobletData getJobletData(Long jobletDataId){
		// mysql has a bug that returns the lastest auto-increment row when queried for null
		if(jobletDataId == null){
			return null;
		}// avoid querying for null
		return getJobRouter().jobletData.get(new JobletDataKey(jobletDataId), null);
	}

	public void handleJobletInterruption(Joblet joblet, boolean rateLimited){
		joblet.setStatus(JobletStatus.created);
		joblet.setReservedBy(null);
		joblet.setReservedAt(null);
		datarouter.run(new UpdateJobletAndQueue(joblet, true, datarouter, getJobRouter(), rateLimited));
		logger.warn("interrupted "+joblet.getKey()+", set status=created, reservedBy=null, reservedAt=null");
	}

	public void handleJobletError(Joblet joblet, boolean rateLimited, Exception exception, String location){
		joblet.setNumFailures(joblet.getNumFailures() + 1);
		if(joblet.getNumFailures() < joblet.getMaxFailures()){
			joblet.setStatus(JobletStatus.created);
		}else{
			joblet.setStatus(JobletStatus.failed);
		}
		ExceptionRecord exceptionRecord = exceptionRecorder.tryRecordException(exception, location,
				JobExceptionCategory.JOBLET);
		joblet.setExceptionRecordId(exceptionRecord.getKey().getId());
		joblet.setReservedBy(null);
		joblet.setReservedAt(null);
		datarouter.run(new UpdateJobletAndQueue(joblet, true, datarouter, getJobRouter(), rateLimited));
	}

	public void handleJobletCompletion(Joblet joblet, boolean decrementQueueIfRateLimited, boolean rateLimited){
		datarouter.run(new DeleteJoblet(datarouter, joblet, getJobRouter(), rateLimited));
	}

	public FeedImportJoblet createAndSaveFeedImportJoblet(String feedId){
		FeedImportJoblet jobletProcess = new FeedImportJoblet(feedId);
		putJobletAndJobletData(jobletProcess.getJoblet());
		return jobletProcess;
	}

	public void putJobletAndJobletData(Joblet joblet){
		getJobRouter().jobletData.put(joblet.getJobletData(), null);
		joblet.setJobletDataId(joblet.getJobletData().getId());
		getJobRouter().joblet.put(joblet, null);
	}

	public void submitJoblets(Collection<? extends JobletProcess> jobletsProcesses){
		List<JobletData> jobletDatas = new ArrayList<>();
		Map<Joblet, JobletData> jobletDataByJoblet = new TreeMap<>();
		for(JobletProcess jobletProcess : jobletsProcesses){
			JobletData jobletData = jobletProcess.getJobletData();
			jobletDatas.add(jobletData);
			jobletDataByJoblet.put(jobletProcess.getJoblet(), jobletData);
		}
		JOB_ROUTER.jobletData.putMulti(jobletDatas, null);
		List<Joblet> joblets = new ArrayList<>();
		for(JobletProcess jobletProcess : jobletsProcesses){
			Joblet joblet = jobletProcess.getJoblet();
			JobletData jobletData = jobletDataByJoblet.get(joblet);
			joblet.setJobletDataId(jobletData.getId());
			joblets.add(joblet);
		}
		JOB_ROUTER.joblet.putMulti(joblets, null);
	}

	public void putMultiJobletAndJobletData(Collection<? extends JobletProcess> jobletsProcesses){
		Collection<JobletData> datas = new ArrayList<>();
		for(JobletProcess jobletProcess : IterableTool.nullSafe(jobletsProcesses)){
			Joblet joblet = jobletProcess.getJoblet();
			JobletData jobletData = joblet.getJobletData();
			datas.add(jobletData);
		}
		getJobRouter().jobletData.putMulti(datas, null);

		List<Joblet> joblets = new ArrayList<>();
		for(JobletProcess jobletProcess : IterableTool.nullSafe(jobletsProcesses)){
			Joblet joblet = jobletProcess.getJoblet();
			joblet.setJobletDataId(joblet.getJobletData().getId());
			joblets.add(joblet);
		}
		getJobRouter().joblet.putMulti(joblets, null);
	}

	public void setJobletsRunningOnServerToCreated(JobletType jobletType, String serverName, boolean rateLimited){
		Iterable<Joblet> joblets = getJobRouter().joblet.scan(null, null);
		String serverNamePrefix = serverName + "_";//don't want joblet1 to include joblet10
		List<Joblet> jobletsToReset = Joblet.filterByTypeStatusReservedByPrefix(joblets, jobletType,
				JobletStatus.running, serverNamePrefix);
		logger.warn("found "+CollectionTool.size(jobletsToReset)+" joblets to reset");

		for(Joblet j : jobletsToReset){
			handleJobletInterruption(j, rateLimited);
		}
	}

	/**
	 * Create joblets and set appropriate image statuses
	 */
	public int queueJoblets(List<BaseSearchImage<?,?>> images, Feed feed, int batchSequence, PhaseTimer timer){
		// create the ImageCachingJoblets
		List<List<BaseSearchImage<?,?>>> batches = BatchTool.getBatches(images, JobletType.ImageCachingJoblet
				.getBatchSize());
		List<ImageCachingJoblet> joblets = new LinkedList<>();
		for(List<BaseSearchImage<?,?>> batch : batches){
			ImageCachingJoblet joblet = new ImageCachingJoblet(BaseSearchImage.getKeys(batch), feed, batchSequence++,
					searchImageDao);
			joblets.add(joblet);
		}
		putMultiJobletAndJobletData(joblets);

		if(timer!=null){
			timer.add("inserted " + joblets.size() + " joblets to queue:" + feed.getId());
		}

		//mark the images as queued
		try {
			List<? extends PrimaryKey<?>> keys = BaseSearchImage.getKeys(images);
			datarouter.run(new UpdateSearchImageStatus(datarouter, jdbcFieldCodecFactory, searchRouter, keys,
					CachedImageStatus.AWAITING_DOWNLOAD));
			if(timer!=null){
				timer.add("marked "+CollectionTool.size(images)+" ids");
			}
		} catch (Exception e) {
			logger.warn("Error updating SearchImageStatuses after creating " +
					"ImageCachingJoblets.  " +
					"WARNING - PROBABLY CREATING REPEAT JOBLETS", e);
		}

		return batchSequence;
	}

	/**
	 * Create a joblet and set image statuses for provided images
	 * @param feed
	 * @param images List of images where i[n].feed == i[n+1].feed
	 */
	public void queueImageDownloadJoblet(Feed feed, List<BaseSearchImage<?,?>> images){
		queueJoblets(images, feed, 0, null);
	}

	public List<JobletSummary> getJobletSummaries(boolean slaveOk){
		Iterable<Joblet> scanner = getJobRouter().joblet.scan(null, new Config().setSlaveOk(slaveOk));
		return Joblet.getJobletCountsCreatedByType(scanner);
	}

	public List<JobletSummary> getJobletSummariesForTable(String whereStatus, boolean includeQueueId){
		return datarouter.run(new GetJobletStatuses(whereStatus, includeQueueId, datarouter, JOB_ROUTER));
	}

	public boolean jobletExistsWithTypeAndStatus(JobletType jobletType, JobletStatus jobletStatus){
		JobletKey key = new JobletKey(jobletType, null, null, null);
		Range<JobletKey> range = new Range<>(key, true, key, true);
		Config config = new Config().setIterateBatchSize(50);
		for(Joblet joblet : JOB_ROUTER.joblet.scan(range, config)){
			if(jobletStatus == joblet.getStatus()){
				return true;
			}
		}
		return false;
	}

}
