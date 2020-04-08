/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.joblet.model;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import io.datarouter.joblet.codec.JobletCodec;
import io.datarouter.joblet.enums.JobletPriority;
import io.datarouter.joblet.storage.jobletdata.JobletData;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.util.HashMethods;
import io.datarouter.util.iterable.IterableTool;
import io.datarouter.util.number.RandomTool;

public class JobletPackage{

	private final JobletRequest jobletRequest;
	private final JobletData jobletData;

	public JobletPackage(JobletRequest jobletRequest, JobletData jobletData){
		this.jobletRequest = Objects.requireNonNull(jobletRequest);
		this.jobletData = jobletData;//handle unexpected nulls later
	}

	public void updateJobletDataIdReference(){
		getJobletRequest().setJobletDataId(getJobletData().getKey().getId());
	}

	public JobletRequest getJobletRequest(){
		return jobletRequest;
	}

	public JobletData getJobletData(){
		return jobletData;
	}

	/*-------------- static -----------------*/

	public static <P> JobletPackage createUnchecked(JobletType<?> uncheckedJobletType, JobletPriority priority,
			boolean restartable, String queueId, String groupId, P params){
		@SuppressWarnings("unchecked")
		JobletType<P> jobletType = (JobletType<P>)uncheckedJobletType;
		return create(jobletType, priority, restartable, queueId, groupId, params);
	}

	public static <P> JobletPackage create(JobletType<P> jobletType, JobletPriority priority, boolean restartable,
			String queueId, String groupId, P params){
		int batchSequence = RandomTool.nextPositiveInt();
		return createDetailed(jobletType, priority, new Date(), batchSequence, restartable, queueId, groupId, params);
	}

	public static <P> JobletPackage createDetailed(JobletType<P> jobletType, JobletPriority priority, Date dateCreated,
			int batchSequence, boolean restartable, String queueId, String groupId, P params){
		JobletCodec<P> codec = jobletType.getCodecSupplier().get();

		//build JobletData
		String encodedParams = codec.marshallData(params);
		JobletData data = new JobletData(encodedParams);

		//build JobletRequest
		JobletRequest request = new JobletRequest(jobletType, priority, dateCreated, batchSequence, restartable,
				getDataSignature(jobletType, params));
		request.setQueueId(queueId);
		request.setGroupId(groupId);
		request.setNumItems(codec.calculateNumItems(params));

		return new JobletPackage(request, data);
	}

	public static List<JobletRequest> getJobletRequests(Collection<JobletPackage> jobletPackages){
		return IterableTool.nullSafeMap(jobletPackages, JobletPackage::getJobletRequest);
	}

	public static List<JobletData> getJobletDatas(Collection<JobletPackage> jobletPackages){
		return IterableTool.nullSafeMap(jobletPackages, JobletPackage::getJobletData);
	}

	public static <P> P unmarshallJobletData(JobletType<P> jobletType, JobletPackage jobletPackage){
		JobletCodec<P> jobletCodec = jobletType.getCodecSupplier().get();
		return jobletCodec.unmarshallData(jobletPackage.getJobletData().getData());
	}

	public static <P> Long getDataSignature(JobletType<P> jobletType, P params){
		JobletCodec<P> codec = jobletType.getCodecSupplier().get();
		String encodedParams = codec.marshallData(params);
		return HashMethods.longMd5DjbHash(encodedParams);
	}

}
