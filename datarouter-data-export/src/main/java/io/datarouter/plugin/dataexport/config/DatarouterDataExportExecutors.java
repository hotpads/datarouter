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
package io.datarouter.plugin.dataexport.config;

import javax.inject.Singleton;

import io.datarouter.util.concurrent.ScalingThreadPoolExecutor;

public class DatarouterDataExportExecutors{

	@Singleton
	public static class DatabeanExportPrefetchExecutor extends ScalingThreadPoolExecutor{
		public DatabeanExportPrefetchExecutor(){
			super("databeanExportPrefetch", 10);
		}
	}

	@Singleton
	public static class DatabeanExportWriteParallelExecutor extends ScalingThreadPoolExecutor{
		public DatabeanExportWriteParallelExecutor(){
			super("databeanExportWriteParallel", 100);
		}
	}

	@Singleton
	public static class DatabeanImportScanChunksExecutor extends ScalingThreadPoolExecutor{
		public DatabeanImportScanChunksExecutor(){
			super("databeanImportScanChunks", 100);
		}
	}

	@Singleton
	public static class DatabeanImportPutMultiExecutor extends ScalingThreadPoolExecutor{
		public DatabeanImportPutMultiExecutor(){
			super("databeanImportPutMulti", 100);
		}
	}

}
