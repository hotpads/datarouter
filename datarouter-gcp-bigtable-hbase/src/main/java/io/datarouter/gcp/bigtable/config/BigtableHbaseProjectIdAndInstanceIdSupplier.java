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
package io.datarouter.gcp.bigtable.config;

public interface BigtableHbaseProjectIdAndInstanceIdSupplier{

	public static class BigtableHbaseProjectIdAndInstanceId{

		public final String projectId;
		public final String instanceId;

		public BigtableHbaseProjectIdAndInstanceId(String projectId, String instanceId){
			this.projectId = projectId;
			this.instanceId = instanceId;
		}

	}

	BigtableHbaseProjectIdAndInstanceId getBigtableHbaseProjectIdAndInstanceId();

	class NoOpBigtableProjectIdAndInstanceIdSupplier implements BigtableHbaseProjectIdAndInstanceIdSupplier{

		@Override
		public BigtableHbaseProjectIdAndInstanceId getBigtableHbaseProjectIdAndInstanceId(){
			return new BigtableHbaseProjectIdAndInstanceId("", "");
		}

	}

}
