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
package io.datarouter.job.detached;

import java.util.Objects;

public class DetachedJobResource{

	public static final Integer DEFAULT_MAX_RAM_PERCENTAGE = 65;

	private final int cpuMilli;
	private final int memoryMb;
	private final int maxRamPercentage;
	private final String targetCluster;

	private DetachedJobResource(int cpuMilli, int memoryMb, int maxRamPercentage, String targetCluster){
		this.cpuMilli = cpuMilli;
		this.memoryMb = memoryMb;
		this.maxRamPercentage = maxRamPercentage;
		this.targetCluster = targetCluster;
	}

	public int getCpuMilli(){
		return cpuMilli;
	}

	public int getMemoryMb(){
		return memoryMb;
	}

	public int getMaxRamPercentage(){
		return maxRamPercentage;
	}

	public String getTargetCluster(){
		return targetCluster;
	}

	public static class DetachedJobResourceBuilder{
		private Integer cpuMilli = null;
		private Integer memoryMb = null;
		private Integer maxRamPercentage = DEFAULT_MAX_RAM_PERCENTAGE;
		private String targetCluster = null;

		public DetachedJobResourceBuilder withResource(DetachedJobResource resource){
			this.cpuMilli = resource.getCpuMilli();
			this.memoryMb = resource.getMemoryMb();
			this.maxRamPercentage = resource.getMaxRamPercentage();
			this.targetCluster = resource.getTargetCluster();
			return this;
		}

		public DetachedJobResourceBuilder withCpuMilli(Integer cpuMilli){
			this.cpuMilli = cpuMilli;
			return this;
		}

		public DetachedJobResourceBuilder withMemoryMb(Integer memoryMb){
			this.memoryMb = memoryMb;
			return this;
		}

		public DetachedJobResourceBuilder withMaxRamPercentage(Integer maxRamPercentage){
			this.maxRamPercentage = maxRamPercentage;
			return this;
		}

		public DetachedJobResourceBuilder withTargetCluster(String cluster){
			this.targetCluster = cluster;
			return this;
		}

		public DetachedJobResource build(){
			Objects.requireNonNull(cpuMilli, "cpuMilli");
			Objects.requireNonNull(memoryMb, "memoryMb");
			return new DetachedJobResource(cpuMilli, memoryMb, maxRamPercentage, targetCluster);
		}
	}

}
