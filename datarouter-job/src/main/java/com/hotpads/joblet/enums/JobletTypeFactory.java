package com.hotpads.joblet.enums;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.joblet.Joblet;
import com.hotpads.joblet.JobletPackage;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.databean.JobletRequestKey;

@Singleton
public class JobletTypeFactory{

	private final List<JobletType<?>> allTypes;
	private final JobletType<?> sampleType;
	private final Map<Integer,JobletType<?>> typeByPersistentInt;
	private final Map<String,JobletType<?>> typeByPersistentString;
	private final Map<String,JobletType<?>> typeByShortQueueName;
	private final Set<JobletType<?>> typesCausingScaling;

	public JobletTypeFactory(Collection<JobletType<?>> types){
		this.allTypes = new ArrayList<>(types);
		this.sampleType = DrCollectionTool.getFirst(types);
		this.typeByPersistentInt = new HashMap<>();
		this.typeByPersistentString = new HashMap<>();
		this.typeByShortQueueName = new HashMap<>();
		for(JobletType<?> type : types){
			//error on duplicate persistentInts
			int persistentInt = type.getPersistentInt();
			if(typeByPersistentInt.containsKey(persistentInt)){
				throw new RuntimeException(type.getAssociatedClass() + " duplicates persistentInt[" + persistentInt
						+ "] of " + typeByPersistentInt.get(persistentInt).getAssociatedClass());
			}
			typeByPersistentInt.put(type.getPersistentInt(), type);

			// error on duplicate persistentStrings
			String persistentString = type.getPersistentString();
			if(typeByPersistentString.containsKey(persistentString)){
				throw new RuntimeException(type.getAssociatedClass() + " duplicates persistentString["
						+ persistentString + "] of " + typeByPersistentString.get(persistentString)
								.getAssociatedClass());
			}
			typeByPersistentString.put(type.getPersistentString(), type);

			// error on duplicate shortQueueNames
			String shortQueueName = type.getShortQueueName();
			if(typeByShortQueueName.containsKey(shortQueueName)){
				throw new RuntimeException(type.getAssociatedClass() + " duplicates shortQueueName["
						+ shortQueueName + "] of " + typeByShortQueueName.get(shortQueueName)
								.getAssociatedClass());
			}
			typeByShortQueueName.put(type.getShortQueueName(), type);
		}
		this.typesCausingScaling = types.stream()
				.filter(JobletType::causesScaling)
				.collect(Collectors.toSet());
	}


	/*------------------ get/set ---------------------*/

	public List<JobletType<?>> getAllTypes(){
		return allTypes;
	}

	public JobletType<?> getSampleType(){
		return sampleType;
	}

	public Set<JobletType<?>> getTypesCausingScaling(){
		return typesCausingScaling;
	}

	/*--------------------- parse persistent string ----------------*/

	public JobletType<?> fromJoblet(Joblet<?> joblet){
		return joblet == null ? null : fromJobletRequest(joblet.getJobletRequest());
	}

	public JobletType<?> fromJobletPackage(JobletPackage jobletPackage){
		return jobletPackage == null ? null : fromJobletRequest(jobletPackage.getJobletRequest());
	}

	public JobletType<?> fromJobletRequest(JobletRequest jobletRequest){
		return jobletRequest == null ? null : fromJobletRequestKey(jobletRequest.getKey());
	}

	public JobletType<?> fromJobletRequestKey(JobletRequestKey jobletKey){
		return jobletKey == null ? null : fromPersistentInt(jobletKey.getTypeCode());
	}

	public JobletType<?> fromPersistentInt(Integer typeCode){
		return typeByPersistentInt.get(typeCode);
	}

	public JobletType<?> fromPersistentString(String string){
		return typeByPersistentString.get(string);
	}

}
