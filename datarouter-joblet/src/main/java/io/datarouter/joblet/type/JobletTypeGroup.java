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
package io.datarouter.joblet.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.datarouter.joblet.type.JobletType.JobletTypeBuilder;

public class JobletTypeGroup{

	private final Map<String,JobletType<?>> types = new TreeMap<>();

	public <P> JobletType<P> buildAndRegister(JobletTypeBuilder<P> builder){
		return register(builder.build());
	}

	public <P> JobletType<P> register(JobletType<P> type){
		types.put(type.getPersistentString(), type);
		return type;
	}

	public List<JobletType<?>> getAll(){
		return new ArrayList<>(types.values());
	}

}
