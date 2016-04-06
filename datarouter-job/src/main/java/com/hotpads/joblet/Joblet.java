package com.hotpads.joblet;

import java.util.Collection;
import java.util.List;

import com.hotpads.joblet.databean.JobletData;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.util.core.stream.StreamTool;

public interface Joblet<P>
extends JobletCodec<P>{

	JobletRequest getJobletRequest();
	void setJoblet(JobletRequest jobletRequest);

	void setJobletData(JobletData jobletData);
	JobletData getJobletData();

	void setData(String data);
	String getData();

	default void updateJobletDataIdReference(){
		getJobletRequest().setJobletDataId(getJobletData().getId());
	}

	void setJobletParams(P params);
//	T getJobletParams();
	Long process();


	/*-------------- static -----------------*/

	static List<JobletRequest> getJoblets(Collection<? extends Joblet<?>> jobletProcesses){
		return StreamTool.map(jobletProcesses, Joblet::getJobletRequest);
	}

	static List<JobletData> getJobletDatas(Collection<? extends Joblet<?>> jobletProcesses){
		return StreamTool.map(jobletProcesses, Joblet::getJobletData);
	}

}
