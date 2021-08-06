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
package io.datarouter.client.hbase.util;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.util.serialization.GsonTool;

public class HBaseTableTool{

	public static Result[] getUnchecked(Table table, List<Get> gets){
		try(var $ = TracerTool.startSpan(table.getName() + " get", TraceSpanGroupType.DATABASE)){
			TracerTool.appendToSpanInfo("gets", gets.size());
			Result[] results = table.get(gets);
			TracerTool.appendToSpanInfo("results", results.length);
			return results;
		}catch(IOException e){
			throw new DataAccessException(e);
		}
	}

	public static void deleteUnchecked(Table table, List<Delete> deletes){
		try(var $ = TracerTool.startSpan(table.getName() + " delete", TraceSpanGroupType.DATABASE)){
			TracerTool.appendToSpanInfo("deletes", deletes.size());
			table.delete(deletes);
		}catch(IOException e){
			throw new DataAccessException(e);
		}
	}

	public static ResultScanner getResultScanner(Table table, Scan scan) throws IOException{
		String start = Bytes.toStringBinary(scan.getStartRow());
		try(var $ = TracerTool.startSpan(table.getName() + " getScanner", TraceSpanGroupType.DATABASE)){
			TracerTool.appendToSpanInfo("start", start);
			return table.getScanner(scan);
		}catch(IOException e){
			throw new IOException("start=" + start
					+ " stop=" + Bytes.toStringBinary(scan.getStopRow())
					+ " scan=" + GsonTool.GSON.toJson(scan), e);
		}
	}

}
