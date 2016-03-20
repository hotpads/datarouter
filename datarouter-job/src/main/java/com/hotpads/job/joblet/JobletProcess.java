package com.hotpads.job.joblet;

import com.hotpads.config.job.databean.Joblet;
import com.hotpads.config.job.databean.JobletData;

public interface JobletProcess{

	Joblet getJoblet();
	void setJoblet(Joblet joblet);

	void setJobletData(JobletData jobletData);
	JobletData getJobletData();

    void marshallData();
    void unmarshallData();
    void unmarshallDataIfNotAlready();

    int calculateNumItems();
    int calculateNumTasks();

	Long process();

}
