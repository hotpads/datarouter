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
package io.datarouter.job.detached;

public class DetachedJobResource{

	private final Integer cpuMilli;
	private final Integer memoryMb;

	private DetachedJobResource(Integer cpuMilli, Integer memoryMb){
		this.cpuMilli = cpuMilli;
		this.memoryMb = memoryMb;
	}

	public Integer getCpuMilli(){
		return cpuMilli;
	}

	public Integer getMemoryMb(){
		return memoryMb;
	}

	public static class DetachedJobResourceBuilder{
		private Integer cpuMilli = null;
		private Integer memoryMb = null;

		public DetachedJobResourceBuilder withCpuMilli(Integer cpuMilli){
			this.cpuMilli = cpuMilli;
			return this;
		}

		public DetachedJobResourceBuilder withMemoryMb(Integer memoryMb){
			this.memoryMb = memoryMb;
			return this;
		}

		public DetachedJobResource build(){
			return new DetachedJobResource(cpuMilli, memoryMb);
		}
	}

}
