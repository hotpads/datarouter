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
package io.datarouter.nodewatch.shadowtable.config;

import io.datarouter.util.concurrent.ScalingThreadPoolExecutor;
import jakarta.inject.Singleton;

public class DatarouterShadowTableExecutors{

	@Singleton
	public static class ShadowTableExportReadExecutor extends ScalingThreadPoolExecutor{
		public ShadowTableExportReadExecutor(){
			super("ShadowTableExportRead", 1_000);
		}
	}

	@Singleton
	public static class ShadowTableRangeWriteExecutor extends ScalingThreadPoolExecutor{
		public ShadowTableRangeWriteExecutor(){
			super("ShadowTableRangeWrite", 1_000);
		}
	}

	@Singleton
	public static class ShadowTableRangeReadExecutor extends ScalingThreadPoolExecutor{
		public ShadowTableRangeReadExecutor(){
			super("ShadowTableRangeRead", 1_000);
		}
	}

	@Singleton
	public static class ShadowTableRangeDecodeExecutor extends ScalingThreadPoolExecutor{
		public ShadowTableRangeDecodeExecutor(){
			super("ShadowTableRangeDecode", 1_000);
		}
	}

	@Singleton
	public static class ShadowTableConcatenatePrefetchExecutor extends ScalingThreadPoolExecutor{
		public ShadowTableConcatenatePrefetchExecutor(){
			super("ShadowTableConcatenatePrefetch", 1_000);
		}
	}

	@Singleton
	public static class ShadowTableCombinePrefetchExecutor extends ScalingThreadPoolExecutor{
		public ShadowTableCombinePrefetchExecutor(){
			super("ShadowTableCombinePrefetch", 1_000);
		}
	}

	@Singleton
	public static class ShadowTableTableEncodeExecutor extends ScalingThreadPoolExecutor{
		public ShadowTableTableEncodeExecutor(){
			super("ShadowTableTableEncode", 1_000);
		}
	}

	@Singleton
	public static class ShadowTableTableWriteExecutor extends ScalingThreadPoolExecutor{
		public ShadowTableTableWriteExecutor(){
			super("ShadowTableTableWrite", 1_000);
		}
	}

}
