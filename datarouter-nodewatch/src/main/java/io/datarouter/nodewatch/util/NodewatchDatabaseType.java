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
package io.datarouter.nodewatch.util;

import java.util.Optional;

import io.datarouter.bytes.ByteLength;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientType;
import io.datarouter.util.time.DurationTool;

public enum NodewatchDatabaseType{

	AURORA(
			"mysql",
			false,
			100,
			1.4),// IO's billed separately
	BIGTABLE_SSD(
			"bigtable",
			true,
			170,
			.4),
	SPANNER(
			"spanner",
			false,
			300,
			1.4);

	public final String clientTypeName;
	public final boolean storesColumnNames;
	public final double dollarsPerTiBPerMonth;
	public final double storageMultiplier;// Blunt multiplier for compression.  Could be refined.

	private NodewatchDatabaseType(
			String clientTypeName,
			boolean storesColumnNames,
			double dollarsPerTiBPerMonth,
			double storageMultiplier){
		this.clientTypeName = clientTypeName;
		this.storesColumnNames = storesColumnNames;
		this.dollarsPerTiBPerMonth = dollarsPerTiBPerMonth;
		this.storageMultiplier = storageMultiplier;
	}

	public static Optional<NodewatchDatabaseType> findPrice(ClientType<?,?> clientType){
		return Scanner.of(values())
				.include(value -> value.clientTypeName.equals(clientType.getName()))
				.findFirst();
	}

	public Optional<Double> findYearlyNodeCost(ByteLength storage){
		if(this == BIGTABLE_SSD){
			return Optional.of(BigtableNodeCost.yearlyNodeCostForStorage(storage));
		}
		if(this == SPANNER){
			return Optional.of(SpannerNodeCost.yearlyNodeCostForStorage(storage));
		}
		return Optional.empty();
	}

	public Optional<Double> findMonthlyNodeCost(ByteLength storage){
		return findYearlyNodeCost(storage)
				.map(yearlyDollars -> yearlyDollars / DurationTool.AVG_MONTHS_PER_YEAR);
	}

	public double dollarsPerTiBPerYear(){
		return 12 * dollarsPerTiBPerMonth;
	}

	public class BigtableNodeCost{
		private static final ByteLength STORAGE_PER_NODE = ByteLength.ofTiB(5);
		private static final double MAX_NODE_CPU_UTILIZATION = .8;
		private static final double MONTHLY_NODE_COST = 476;

		public static double monthlyNodeCostForStorage(ByteLength storage){
			double minNumNodes = storage.toBytesDouble() / STORAGE_PER_NODE.toBytesDouble();
			double actualNumNodes = minNumNodes / MAX_NODE_CPU_UTILIZATION;
			return actualNumNodes * MONTHLY_NODE_COST;
		}

		public static double yearlyNodeCostForStorage(ByteLength storage){
			return 12 * monthlyNodeCostForStorage(storage);
		}
	}

	public class SpannerNodeCost{
		private static final ByteLength STORAGE_PER_NODE = ByteLength.ofTiB(4);
		private static final double MAX_NODE_CPU_UTILIZATION = .8;
		private static final double MONTHLY_NODE_COST = 658;

		public static double monthlyNodeCostForStorage(ByteLength storage){
			double minNumNodes = storage.toBytesDouble() / STORAGE_PER_NODE.toBytesDouble();
			double actualNumNodes = minNumNodes / MAX_NODE_CPU_UTILIZATION;
			return actualNumNodes * MONTHLY_NODE_COST;
		}

		public static double yearlyNodeCostForStorage(ByteLength storage){
			return 12 * monthlyNodeCostForStorage(storage);
		}
	}


}
