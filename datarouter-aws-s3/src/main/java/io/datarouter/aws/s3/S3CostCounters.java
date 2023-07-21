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

import io.datarouter.instrumentation.cost.CloudPriceType;
import io.datarouter.instrumentation.cost.CostCounters;

public class S3CostCounters{

	public static void read(){
		String id = CloudPriceType.BLOB_READ_AWS.id;
		CostCounters.incInput(id, 1);
		CostCounters.incNanos(id, CloudPriceType.BLOB_READ_AWS.nanoDollars);
	}

	public static void list(){
		String id = CloudPriceType.BLOB_LIST_AWS.id;
		CostCounters.incInput(id, 1);
		CostCounters.incNanos(id, CloudPriceType.BLOB_LIST_AWS.nanoDollars);
	}

	public static void write(){
		String id = CloudPriceType.BLOB_WRITE_AWS.id;
		CostCounters.incInput(id, 1);
		CostCounters.incNanos(id, CloudPriceType.BLOB_WRITE_AWS.nanoDollars);
	}

}
