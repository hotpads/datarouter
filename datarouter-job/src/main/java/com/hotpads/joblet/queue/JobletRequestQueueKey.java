package com.hotpads.joblet.queue;

import java.util.Objects;

import com.hotpads.datarouter.util.core.DrComparableTool;
import com.hotpads.joblet.enums.JobletPriority;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.util.core.lang.ClassTool;

public class JobletRequestQueueKey implements Comparable<JobletRequestQueueKey>{

	private final JobletType<?> type;
	private final JobletPriority priority;

	public JobletRequestQueueKey(JobletType<?> type, JobletPriority priority){
		this.type = type;
		this.priority = priority;
	}

	public String getQueueName(){
		return type.getPersistentString() + "-" + priority.getComparableName();
	}

	/*---------------- Object ------------------*/

	@Override
	public boolean equals(Object obj){
		if(ClassTool.differentClass(this, obj)){
			return false;
		}
		JobletRequestQueueKey other = (JobletRequestQueueKey)obj;
		return Objects.equals(type, other.type)
				&& Objects.equals(priority, other.priority);
	}

	@Override
	public int hashCode(){
		return Objects.hash(type, priority);
	}

	/*------------- Comparable -------------------*/

	@Override
	public int compareTo(JobletRequestQueueKey other){
		int diff = DrComparableTool.nullFirstCompareTo(this.type, other.type);
		if(diff != 0){
			return diff;
		}
		return DrComparableTool.nullFirstCompareTo(this.priority, other.priority);
	}

	/*-------------- get/set ------------------------*/

	public JobletType<?> getType(){
		return type;
	}

	public JobletPriority getPriority(){
		return priority;
	}

}
