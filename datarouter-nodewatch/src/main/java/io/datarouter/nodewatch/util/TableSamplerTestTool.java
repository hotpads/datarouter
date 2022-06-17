/*
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
package io.datarouter.nodewatch.util;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.joblet.model.JobletPackage;
import io.datarouter.joblet.storage.jobletdata.JobletData;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.nodewatch.joblet.TableSpanSamplerJoblet;
import io.datarouter.nodewatch.joblet.TableSpanSamplerJoblet.TableSpanSamplerJobletCodec;
import io.datarouter.nodewatch.joblet.TableSpanSamplerJoblet.TableSpanSamplerJobletParams;
import io.datarouter.nodewatch.storage.tablesample.TableSample;

public class TableSamplerTestTool{

	public static List<TableSample> executeJobletsAndCollectSamples(
			DatarouterInjector injector,
			List<JobletPackage> jobletPackages){
		List<TableSample> results = new ArrayList<>();
		for(JobletData jobletData : JobletPackage.getJobletDatas(jobletPackages)){
			TableSpanSamplerJoblet joblet = injector.getInstance(TableSpanSamplerJoblet.class);
			JobletRequest jobletRequest = new JobletRequest();
			jobletRequest.getKey().setCreated(System.currentTimeMillis());// this field referenced by the joblet
			joblet.setJobletRequest(jobletRequest);
			TableSpanSamplerJobletParams params = new TableSpanSamplerJobletCodec().unmarshallData(
					jobletData.getData());
			joblet.setJobletParams(params);
			joblet.process();
			results.addAll(joblet.getSamples());
		}
		return results;
	}

}
