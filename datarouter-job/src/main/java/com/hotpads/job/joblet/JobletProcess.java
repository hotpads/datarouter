package com.hotpads.job.joblet;

import java.util.Collection;
import java.util.List;

import com.hotpads.config.job.databean.Joblet;
import com.hotpads.config.job.databean.JobletData;
import com.hotpads.util.core.stream.StreamTool;

public interface JobletProcess{

	Joblet getJoblet();
	void setJoblet(Joblet joblet);

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

	static List<Joblet> getJoblets(Collection<? extends JobletProcess> jobletProcesses){
		return StreamTool.map(jobletProcesses, JobletProcess::getJoblet);
	}

	static List<JobletData> getJobletDatas(Collection<? extends JobletProcess> jobletProcesses){
		return StreamTool.map(jobletProcesses, JobletProcess::getJobletData);
	}

}
