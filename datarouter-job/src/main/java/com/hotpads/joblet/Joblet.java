package com.hotpads.joblet;

import java.util.Collection;
import java.util.List;

import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.databean.JobletData;
import com.hotpads.util.core.stream.StreamTool;

public interface Joblet{

	JobletRequest getJoblet();
	void setJoblet(JobletRequest joblet);

	void setJobletData(JobletData jobletData);
	JobletData getJobletData();

	void setData(String data);
	String getData();

	default void updateJobletDataIdReference(){
		getJoblet().setJobletDataId(getJobletData().getId());
	}

    void marshallData();
    void unmarshallData();
    void unmarshallDataIfNotAlready();

    int calculateNumItems();
    int calculateNumTasks();

	Long process();


	/*-------------- static -----------------*/

	static List<JobletRequest> getJoblets(Collection<? extends Joblet> jobletProcesses){
		return StreamTool.map(jobletProcesses, Joblet::getJoblet);
	}

	static List<JobletData> getJobletDatas(Collection<? extends Joblet> jobletProcesses){
		return StreamTool.map(jobletProcesses, Joblet::getJobletData);
	}

}
