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
package io.datarouter.nodewatch.web;

import static j2html.TagCreator.script;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.datarouter.nodewatch.storage.tablecount.TableCount;
import j2html.tags.specialized.ScriptTag;

public class NodewatchTableHistoryGraph{

	public static final String GRAPH_DIV_ID = "graphdiv";

	private List<TableCount> tableCounts;

	public NodewatchTableHistoryGraph(List<TableCount> tableCounts){
		this.tableCounts = tableCounts;
	}

	public ScriptTag makeDataScript(){
		return script(String.format(DYGRAPH_DATA_FORMAT, makeJsonArray().toString()));
	}

	private JsonArray makeJsonArray(){
		var jsonArray = new JsonArray();
		for(TableCount record : tableCounts){
			JsonObject json = new JsonObject();
			Long date = record.getDateUpdated().toEpochMilli();
			Long rows = record.getNumRows();
			json.addProperty("date", date);
			json.addProperty("rows", rows);
			jsonArray.add(json);
		}
		return jsonArray;
	}

	public final ScriptTag makeGraphScript(){
		return script(DYGRAPH);
	}

	private static final String DYGRAPH_DATA_FORMAT = """
			const DATE_ROWS_DATA = %s.map(({date, rows}) => [new Date(date), rows])
			let DYGRAPH = null
			""";

	// with escaped backslashes
	private static final String DYGRAPH = """
			require(['dygraph'], function(){
				DYGRAPH = new Dygraph(document.getElementById("graphdiv"), DATE_ROWS_DATA, {
					drawPoints: true,
					drawGapEdgePoints: true,
					pointSize: 2,
					fillGraph: true,
					maxNumberWidth: 10,
					axisLabelWidth: 70,
					axisLabelFontSize: 11,
					axes:{
						y: {
							axisLabelWidth: 40,
							axisLabelFormatter(count){
								this.labelY1Memo = this.labelY1Memo || {}
								if(this.labelY1Memo[count]){
									return this.labelY1Memo[count]
								}
								// 1..10..100..1k..10k..100k..1m..10m..100m..1b..10b..100b..1t..10t..100t
								if(count < 1000){
									return count;
								}
								const suffixes = ['', 'k', 'm', 'b', 't']
								const strCount = String(count)
								const power = strCount.replace(/\\..+/, '').length - 1
								const powerFloor3 = Math.floor(power / 3)
								const beforeZeros = count.toFixed(1) / (10 ** (3 * powerFloor3))
								const label = beforeZeros + suffixes[powerFloor3]
								this.labelY1Memo[count] = label
								return label
							}
						}
					}
				})
			})
			""";
}
