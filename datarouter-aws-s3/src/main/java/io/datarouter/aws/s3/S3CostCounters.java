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
package io.datarouter.aws.s3;

import io.datarouter.instrumentation.cost.CostCounters;

public class S3CostCounters{

	// $0.005/k
	private static final long LIST_NANOS = 5_000;
	// $0.0004/k
	private static final long READ_NANOS = 400;
	// $0.005/k
	private static final long WRITE_NANOS = 5_000;

	public static void list(){
		CostCounters.nanos("data", "blob", "s3", "list", LIST_NANOS);
	}

	public static void read(){
		CostCounters.nanos("data", "blob", "s3", "read", READ_NANOS);
	}

	public static void write(){
		CostCounters.nanos("data", "blob", "s3", "write", WRITE_NANOS);
	}

}
