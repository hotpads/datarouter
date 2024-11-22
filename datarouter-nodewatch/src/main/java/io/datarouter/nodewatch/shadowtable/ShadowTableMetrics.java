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
package io.datarouter.nodewatch.shadowtable;

import io.datarouter.instrumentation.metric.Metrics;
import io.datarouter.storage.client.ClientAndTableNames;

public class ShadowTableMetrics{

	private static final String PREFIX = "ShadowTable";
	private static final String EXPORT_ROWS = "export rows";
	private static final String EXPORT_DATABEAN_BYTES = "export databeanBytes";
	private static final String EXPORT_BLOCKFILE_INPUT_BYTES = "export blockfileInputBytes";
	private static final String EXPORT_BLOCKFILE_OUTPUT_BYTES = "export blockfileOutputBytes";

	/*------- count export rows -------*/

	public static void countExportRows(
			String exportName,
			ClientAndTableNames names,
			long numRows){
		countExportRows(numRows, "all");
		countExportRows(numRows, "export", exportName);
		countExportRows(numRows, "client", names.client());
		countExportRows(numRows, "table", names.client(), names.table());
	}

	private static void countExportRows(long value, String... tokens){
		String joinedTokens = String.join(" ", tokens);
		String metricName = String.join(" ", PREFIX, EXPORT_ROWS, joinedTokens);
		Metrics.count(metricName, value);
	}

	/*------- count export databeanBytes -------*/

	public static void countDatabeanBytes(
			String exportName,
			ClientAndTableNames names,
			long numBytes){
		countDatabeanBytes(numBytes, "all");
		countDatabeanBytes(numBytes, "export", exportName);
		countDatabeanBytes(numBytes, "client", names.client());
		countDatabeanBytes(numBytes, "table", names.client(), names.table());
	}

	private static void countDatabeanBytes(long numBytes, String... tokens){
		String joinedTokens = String.join(" ", tokens);
		String metricName = String.join(" ", PREFIX, EXPORT_DATABEAN_BYTES, joinedTokens);
		Metrics.count(metricName, numBytes);
	}

	/*------- count export blockfileInputBytes -------*/

	public static void countBlockfileInputBytes(
			String exportName,
			ClientAndTableNames names,
			long numBytes){
		countBlockfileInputBytes(numBytes, "all");
		countBlockfileInputBytes(numBytes, "export", exportName);
		countBlockfileInputBytes(numBytes, "client", names.client());
		countBlockfileInputBytes(numBytes, "table", names.client(), names.table());
	}

	private static void countBlockfileInputBytes(long numBytes, String... tokens){
		String joinedTokens = String.join(" ", tokens);
		String metricName = String.join(" ", PREFIX, EXPORT_BLOCKFILE_INPUT_BYTES, joinedTokens);
		Metrics.count(metricName, numBytes);
	}

	/*------- count export blockfileOutputBytes -------*/

	public static void countBlockfileOutputBytes(
			String exportName,
			ClientAndTableNames names,
			long numBytes){
		countBlockfileOutputBytes(numBytes, "all");
		countBlockfileOutputBytes(numBytes, "export", exportName);
		countBlockfileOutputBytes(numBytes, "client", names.client());
		countBlockfileOutputBytes(numBytes, "table", names.client(), names.table());
	}

	private static void countBlockfileOutputBytes(long numBytes, String... tokens){
		String joinedTokens = String.join(" ", tokens);
		String metricName = String.join(" ", PREFIX, EXPORT_BLOCKFILE_OUTPUT_BYTES, joinedTokens);
		Metrics.count(metricName, numBytes);
	}

}
