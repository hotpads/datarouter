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
package io.datarouter.instrumentation.cost;

/**
 * Prices for cloud services.
 * These are estimated prices.
 * Factors that affect them:
 *   - Time: they tend to drop over time
 *   - Location: they differ by cloud region
 *   - Usage: price can reduce for higher usage
 *   - Reservations: price can be lower if you pre-pay for an amount
 *   - Taxes: these estimates don't include taxes
 *   - Discounts: large customers might negotiate discounts
 */
public enum CloudPriceType{

	BLOB_READ_AWS("blob read aws", 400),// $0.0004/k
	BLOB_LIST_AWS("blob list aws", 5_000),// $0.005/k
	BLOB_WRITE_AWS("blob write aws", 5_000),// $0.005/k
	MESSAGE_REQUEST_AWS("message request aws", 400),// $0.4/mm
	VM_CPU_MILLIMINUTE_GCP_N2("vm cpu milliMinute gcp n2", 527),// $0.031611 / vCPU hour
	VM_MEMORY_MIBMINUTE_GCP_N2("vm memory mibMinute gcp n2", 71);// $0.004237 / GB hour

	public final String id;
	public final long nanoDollars;

	private CloudPriceType(String name, long nanoDollars){
		this.id = name;
		this.nanoDollars = nanoDollars;
	}

}
