package com.hotpads.joblet;

import java.util.Collection;
import java.util.List;

import com.hotpads.joblet.databean.JobletData;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.enums.JobletPriority;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.util.core.number.RandomTool;
import com.hotpads.util.core.stream.StreamTool;

public class JobletPackage {

	private final JobletRequest joblet;
	private final JobletData jobletData;

	public JobletPackage(JobletRequest joblet, JobletData jobletData) {
		this.joblet = joblet;
		this.jobletData = jobletData;
	}

	public void updateJobletDataIdReference(){
		getJoblet().setJobletDataId(getJobletData().getId());
	}

	public JobletRequest getJoblet() {
		return joblet;
	}

	public JobletData getJobletData(){
		return jobletData;
	}

	/*-------------- static -----------------*/

	public static <P> JobletPackage createUnchecked(JobletType<?> uncheckedJobletType, JobletPriority priority,
			boolean restartable, String queueId, P params){
		@SuppressWarnings("unchecked")
		JobletType<P> jobletType = (JobletType<P>)uncheckedJobletType;
		return create(jobletType, priority, restartable, queueId, params);
	}

	public static <P> JobletPackage create(JobletType<P> jobletType, JobletPriority priority, boolean restartable,
			String queueId, P params){
		int batchSequence = RandomTool.nextPositiveInt();
		return createWithBatchSequence(jobletType, priority, batchSequence, restartable, queueId, params);
	}

	public static <P> JobletPackage createWithBatchSequence(JobletType<P> jobletType, JobletPriority priority,
			int batchSequence, boolean restartable, String queueId, P params){
		JobletCodec<P> codec = jobletType.getCodecSupplier().get();

		//build JobletRequest
		JobletRequest request = new JobletRequest(jobletType, priority, batchSequence, restartable);
		request.setQueueId(queueId);
		request.setNumItems(codec.calculateNumItems(params));
		request.setNumTasks(codec.calculateNumTasks(params));

		//build JobletData
		String encodedParams = codec.marshallData(params);
		JobletData data = new JobletData(encodedParams);

		return new JobletPackage(request, data);
	}

	public static List<JobletRequest> getJobletRequests(Collection<JobletPackage> jobletPackages){
		return StreamTool.map(jobletPackages, JobletPackage::getJoblet);
	}

	public static List<JobletData> getJobletDatas(Collection<JobletPackage> jobletPackages){
		return StreamTool.map(jobletPackages, JobletPackage::getJobletData);
	}

}
