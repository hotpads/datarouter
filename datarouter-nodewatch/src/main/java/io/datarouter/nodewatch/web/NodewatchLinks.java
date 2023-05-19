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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.http.client.utils.URIBuilder;

import io.datarouter.nodewatch.config.DatarouterNodewatchPaths;
import io.datarouter.nodewatch.web.handler.NodewatchTableActionsHandler;
import io.datarouter.nodewatch.web.handler.NodewatchTableHandler;
import io.datarouter.nodewatch.web.handler.NodewatchThresholdEditHandler;
import io.datarouter.web.config.ServletContextSupplier;

@Singleton
public class NodewatchLinks{

	@Inject
	private ServletContextSupplier contextSupplier;
	@Inject
	private DatarouterNodewatchPaths paths;

	public String tables(){
		var uriBuilder = new URIBuilder()
				.setPath(contextSupplier.getContextPath() + paths.datarouter.nodewatch.tables.toSlashedString());
		return uriBuilder.toString();
	}

	public String summary(){
		var uriBuilder = new URIBuilder()
				.setPath(contextSupplier.getContextPath() + paths.datarouter.nodewatch.summary.toSlashedString());
		return uriBuilder.toString();
	}

	public String configs(){
		var uriBuilder = new URIBuilder()
				.setPath(contextSupplier.getContextPath() + paths.datarouter.nodewatch.configs.toSlashedString());
		return uriBuilder.toString();
	}

	public String slowSpans(){
		var uriBuilder = new URIBuilder()
				.setPath(contextSupplier.getContextPath() + paths.datarouter.nodewatch.slowSpans.toSlashedString());
		return uriBuilder.toString();
	}

	public String metadataMigrate(){
		var uriBuilder = new URIBuilder()
				.setPath(contextSupplier.getContextPath()
						+ paths.datarouter.nodewatch.metadata.migrate.toSlashedString());
		return uriBuilder.toString();
	}

	public String table(String clientName, String tableName){
		var uriBuilder = new URIBuilder()
				.setPath(contextSupplier.getContextPath() + paths.datarouter.nodewatch.table.toSlashedString())
				.addParameter(NodewatchTableHandler.P_clientName, clientName)
				.addParameter(NodewatchTableHandler.P_tableName, tableName);
		return uriBuilder.toString();
	}

	public String tableResample(String clientName, String tableName){
		var uriBuilder = new URIBuilder()
				.setPath(contextSupplier.getContextPath() + paths.datarouter.nodewatch.table.resample.toSlashedString())
				.addParameter(NodewatchTableActionsHandler.P_clientName, clientName)
				.addParameter(NodewatchTableActionsHandler.P_tableName, tableName);
		return uriBuilder.toString();
	}

	public String tableDeleteSamples(String clientName, String tableName){
		var uriBuilder = new URIBuilder()
				.setPath(contextSupplier.getContextPath()
						+ paths.datarouter.nodewatch.table.deleteSamples.toSlashedString())
				.addParameter(NodewatchTableActionsHandler.P_clientName, clientName)
				.addParameter(NodewatchTableActionsHandler.P_tableName, tableName);
		return uriBuilder.toString();
	}

	public String tableDeleteAllMetadata(String clientName, String tableName){
		var uriBuilder = new URIBuilder()
				.setPath(contextSupplier.getContextPath()
						+ paths.datarouter.nodewatch.table.deleteAllMetadata.toSlashedString())
				.addParameter(NodewatchTableActionsHandler.P_clientName, clientName)
				.addParameter(NodewatchTableActionsHandler.P_tableName, tableName);
		return uriBuilder.toString();
	}

	public String thresholdEdit(String clientName, String tableName){
		var uriBuilder = new URIBuilder()
				.setPath(contextSupplier.getContextPath()
						+ paths.datarouter.nodewatch.threshold.edit.toSlashedString())
				.addParameter(NodewatchThresholdEditHandler.P_clientName, clientName)
				.addParameter(NodewatchThresholdEditHandler.P_tableName, tableName);
		return uriBuilder.toString();
	}

	public String thresholdDelete(String clientName, String tableName){
		var uriBuilder = new URIBuilder()
				.setPath(contextSupplier.getContextPath()
						+ paths.datarouter.nodewatch.threshold.delete.toSlashedString())
				.addParameter(NodewatchThresholdEditHandler.P_clientName, clientName)
				.addParameter(NodewatchThresholdEditHandler.P_tableName, tableName);
		return uriBuilder.toString();
	}

}
