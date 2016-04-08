package com.hotpads.joblet;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import com.hotpads.joblet.databean.JobletData;
import com.hotpads.joblet.databean.JobletRequest;
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

	public static <T,P> JobletPackage create(JobletType<T> jobletType, Supplier<? extends JobletCodec<P>> codecSupplier,
			int executionOrder, boolean restartable, String queueId, P params){
		int batchSequence = RandomTool.nextPositiveInt();
		JobletRequest request = new JobletRequest(jobletType, executionOrder, batchSequence, restartable);
		request.setQueueId(queueId);
		JobletCodec<P> codec = codecSupplier.get();
		String encodedParams = codec.marshallData(params);
		JobletData data = new JobletData(encodedParams);
		return new JobletPackage(request, data);
	}

	public static List<JobletRequest> getJoblets(Collection<JobletPackage> jobletPackages){
		return StreamTool.map(jobletPackages, JobletPackage::getJoblet);
	}

	public static List<JobletData> getJobletDatas(Collection<JobletPackage> jobletProcesses){
		return StreamTool.map(jobletProcesses, JobletPackage::getJobletData);
	}

}
