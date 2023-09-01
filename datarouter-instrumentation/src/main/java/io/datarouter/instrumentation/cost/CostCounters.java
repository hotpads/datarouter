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

import io.datarouter.instrumentation.count.Counters;

/**
 * For counting the cost of cloud services.
 * "Nanos" are billionths of dollars.
 * Should be viewed as estimates.
 * Factors that affect costs:
 *   - Time: they tend to drop over time
 *   - Location: they differ by cloud region
 *   - Usage: price can reduce for higher usage
 *   - Reservations: price can be lower if you pre-pay for an amount
 *   - Taxes: these estimates don't include taxes
 *   - Discounts: large customers might negotiate discounts
 */
public class CostCounters{

	// Overall Cost counter prefix
	private static final String ROOT = "Cost";

	// Groupings
	private static final String GROUP_TOTAL = "total";
	private static final String GROUP_CATEGORY = "category";// network, data, compute, etc
	private static final String GROUP_TYPE = "type";// cache, messaging, database, etc
	private static final String GROUP_PRODUCT = "product";// sqs, spanner, etc
	private static final String GROUP_SKU = "sku";// s3 list, s3 put, etc

	// Search
	private static final String WILDCARD = ".*";
	public static final String SEARCH_TOTAL = join(ROOT, GROUP_TOTAL);
	public static final String SEARCH_PRODUCT = join(ROOT, GROUP_PRODUCT, WILDCARD);
	public static final String SEARCH_SKU = join(ROOT, GROUP_SKU, WILDCARD);

	/*-------- nanos ---------*/

	public static void nanos(
			String category,
			String type,
			String product,
			String sku,
			long nanos){
		incCost(GROUP_TOTAL, nanos);
		incCost(join(GROUP_CATEGORY, category), nanos);
		incCost(join(GROUP_TYPE, type), nanos);
		incCost(join(GROUP_PRODUCT, product), nanos);
		incCost(join(GROUP_SKU, product, sku), nanos);
	}

	/*-------- cost ----------*/

	private static void incCost(String suffix, long nanos){
		Counters.inc(join(ROOT, suffix), nanos);
	}

	/*------- join ---------*/

	private static String join(String... tokens){
		return String.join(" ", tokens);
	}

}
