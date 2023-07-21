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
package io.datarouter.aws.s3.config;

import io.datarouter.util.concurrent.ScalingThreadPoolExecutor;
import jakarta.inject.Singleton;

public class DatarouterAwsS3Executors{

	@Singleton
	public static class DatarouterS3BucketRegionExecutor extends ScalingThreadPoolExecutor{
		public DatarouterS3BucketRegionExecutor(){
			super("datarouterS3BucketRegion", 100);
		}
	}

	@Singleton
	public static class DatarouterS3BulkCopyReadExecutor extends ScalingThreadPoolExecutor{
		public DatarouterS3BulkCopyReadExecutor(){
			super("datarouterS3BulkCopyRead", 100);
		}
	}

	@Singleton
	public static class DatarouterS3BulkCopyWriteExecutor extends ScalingThreadPoolExecutor{
		public DatarouterS3BulkCopyWriteExecutor(){
			super("datarouterS3BulkCopyWrite", 100);
		}
	}

	@Singleton
	public static class DatarouterS3BulkDeleteExecutor extends ScalingThreadPoolExecutor{
		public DatarouterS3BulkDeleteExecutor(){
			super("datarouterS3BulkDelete", 100);
		}
	}

}
