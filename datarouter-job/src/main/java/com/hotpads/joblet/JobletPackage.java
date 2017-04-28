package com.hotpads.joblet;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.hotpads.joblet.databean.JobletData;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.enums.JobletPriority;
import com.hotpads.joblet.type.JobletType;
import com.hotpads.util.core.number.RandomTool;
import com.hotpads.util.core.stream.StreamTool;

public class JobletPackage{

	private final JobletRequest jobletRequest;
	private final JobletData jobletData;

	public JobletPackage(JobletRequest jobletRequest, JobletData jobletData){
		this.jobletRequest = Objects.requireNonNull(jobletRequest);
		this.jobletData = Objects.requireNonNull(jobletData);
	}

	public void updateJobletDataIdReference(){
		getJobletRequest().setJobletDataId(getJobletData().getId());
	}

	public JobletRequest getJobletRequest(){
		return jobletRequest;
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
		return createDetailed(jobletType, priority, new Date(), batchSequence, restartable, queueId, params);
	}

	public static <P> JobletPackage createDetailed(JobletType<P> jobletType, JobletPriority priority, Date dateCreated,
			int batchSequence, boolean restartable, String queueId, P params){
		JobletCodec<P> codec = jobletType.getCodecSupplier().get();

		//build JobletRequest
		JobletRequest request = new JobletRequest(jobletType, priority, dateCreated, batchSequence, restartable);
		request.setQueueId(queueId);
		request.setNumItems(codec.calculateNumItems(params));
		request.setNumTasks(codec.calculateNumTasks(params));

		//build JobletData
		String encodedParams = codec.marshallData(params);
		JobletData data = new JobletData(encodedParams);

		return new JobletPackage(request, data);
	}

	public static List<JobletRequest> getJobletRequests(Collection<JobletPackage> jobletPackages){
		return StreamTool.map(jobletPackages, JobletPackage::getJobletRequest);
	}

	public static List<JobletData> getJobletDatas(Collection<JobletPackage> jobletPackages){
		return StreamTool.map(jobletPackages, JobletPackage::getJobletData);
	}

	public static <P> P unmarshallJobletData(JobletType<P> jobletType, JobletPackage jobletPackage){
		JobletCodec<P> jobletCodec = jobletType.getCodecSupplier().get();
		return jobletCodec.unmarshallData(
				jobletPackage.getJobletData().getData());
	}

}
