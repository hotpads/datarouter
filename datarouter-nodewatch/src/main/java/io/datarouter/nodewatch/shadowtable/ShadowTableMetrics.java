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
	private static final String COMBINE_ROWS = "combine rows";
	private static final String COMBINE_BYTES_IN = "combine bytesIn";
	private static final String COMBINE_BYTES_OUT = "combine bytesOut";
	private static final String COMBINE_BLOCKS_OUT = "combine blocksOut";
	private static final String COMBINE_BLOCKS_PREFETCHED = "combine blocksPrefetched";

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

	/*------- count combine rows -------*/

	public static void countCombineRows(
			String exportName,
			ClientAndTableNames names,
			long numRows){
		countCombineRows(numRows, "all");
		countCombineRows(numRows, "export", exportName);
		countCombineRows(numRows, "client", names.client());
		countCombineRows(numRows, "table", names.client(), names.table());
	}

	private static void countCombineRows(long value, String... tokens){
		String joinedTokens = String.join(" ", tokens);
		String metricName = String.join(" ", PREFIX, COMBINE_ROWS, joinedTokens);
		Metrics.count(metricName, value);
	}

	/*------- count combine bytes in -------*/

	public static void countCombineBytesIn(
			String exportName,
			ClientAndTableNames names,
			long numBytes){
		countCombineBytesIn(numBytes, "all");
		countCombineBytesIn(numBytes, "export", exportName);
		countCombineBytesIn(numBytes, "client", names.client());
		countCombineBytesIn(numBytes, "table", names.client(), names.table());
	}

	private static void countCombineBytesIn(long value, String... tokens){
		String joinedTokens = String.join(" ", tokens);
		String metricName = String.join(" ", PREFIX, COMBINE_BYTES_IN, joinedTokens);
		Metrics.count(metricName, value);
	}

	/*------- count combine bytes out -------*/

	public static void countCombineBytesOut(
			String exportName,
			ClientAndTableNames names,
			long numBytes){
		countCombineBytesOut(numBytes, "all");
		countCombineBytesOut(numBytes, "export", exportName);
		countCombineBytesOut(numBytes, "client", names.client());
		countCombineBytesOut(numBytes, "table", names.client(), names.table());
	}

	private static void countCombineBytesOut(long value, String... tokens){
		String joinedTokens = String.join(" ", tokens);
		String metricName = String.join(" ", PREFIX, COMBINE_BYTES_OUT, joinedTokens);
		Metrics.count(metricName, value);
	}

	/*------- count combine blocks out -------*/

	public static void countCombineBlocksOut(
			String exportName,
			ClientAndTableNames names,
			long numBlocks){
		countCombineBlocksOut(numBlocks, "all");
		countCombineBlocksOut(numBlocks, "export", exportName);
		countCombineBlocksOut(numBlocks, "client", names.client());
		countCombineBlocksOut(numBlocks, "table", names.client(), names.table());
	}

	private static void countCombineBlocksOut(long value, String... tokens){
		String joinedTokens = String.join(" ", tokens);
		String metricName = String.join(" ", PREFIX, COMBINE_BLOCKS_OUT, joinedTokens);
		Metrics.count(metricName, value);
	}

	/*------- measure combine blocks prefetched -------*/

	public static void measureCombineBlocksPrefetched(
			String exportName,
			ClientAndTableNames names,
			long numBlocks){
		measureCombineBlocksPrefetched(numBlocks, "all");
		measureCombineBlocksPrefetched(numBlocks, "export", exportName);
		measureCombineBlocksPrefetched(numBlocks, "client", names.client());
		measureCombineBlocksPrefetched(numBlocks, "table", names.client(), names.table());
	}

	private static void measureCombineBlocksPrefetched(long value, String... tokens){
		String joinedTokens = String.join(" ", tokens);
		String metricName = String.join(" ", PREFIX, COMBINE_BLOCKS_PREFETCHED, joinedTokens);
		Metrics.measure(metricName, value);
	}

}
