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

import io.datarouter.httpclient.endpoint.link.DatarouterLinkClient;
import io.datarouter.nodewatch.link.NodewatchConfigsLink;
import io.datarouter.nodewatch.link.NodewatchDeleteAllMetadataLink;
import io.datarouter.nodewatch.link.NodewatchMetadataMigrateLink;
import io.datarouter.nodewatch.link.NodewatchSlowSpansLink;
import io.datarouter.nodewatch.link.NodewatchSummaryLink;
import io.datarouter.nodewatch.link.NodewatchTableDeleteSamplesLink;
import io.datarouter.nodewatch.link.NodewatchTableLink;
import io.datarouter.nodewatch.link.NodewatchTableResampleLink;
import io.datarouter.nodewatch.link.NodewatchTableStorageLink;
import io.datarouter.nodewatch.link.NodewatchTablesLink;
import io.datarouter.nodewatch.link.NodewatchThresholdDeleteLink;
import io.datarouter.nodewatch.link.NodewatchThresholdEditLink;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

// Includes local contextPath
@Singleton
public class NodewatchLinks{

	@Inject
	private DatarouterLinkClient linkClient;

	public String tables(){
		return linkClient.toInternalUrl(new NodewatchTablesLink());
	}

	public String summary(){
		return linkClient.toInternalUrl(new NodewatchSummaryLink());
	}

	public String configs(){
		return linkClient.toInternalUrl(new NodewatchConfigsLink());
	}

	public String slowSpans(){
		return linkClient.toInternalUrl(new NodewatchSlowSpansLink());
	}

	public String metadataMigrate(){
		return linkClient.toInternalUrl(new NodewatchMetadataMigrateLink());
	}

	public String table(String clientName, String tableName){
		return linkClient.toInternalUrl(new NodewatchTableLink(clientName, tableName));
	}

	public String tableStorage(String clientName, String tableName){
		return linkClient.toInternalUrl(new NodewatchTableStorageLink(clientName, tableName));
	}

	public String tableResample(String clientName, String tableName){
		return linkClient.toInternalUrl(new NodewatchTableResampleLink(clientName, tableName));
	}

	public String tableDeleteSamples(String clientName, String tableName){
		return linkClient.toInternalUrl(new NodewatchTableDeleteSamplesLink(clientName, tableName));
	}

	public String tableDeleteAllMetadata(String clientName, String tableName){
		return linkClient.toInternalUrl(new NodewatchDeleteAllMetadataLink(clientName, tableName));
	}

	public String thresholdEdit(String clientName, String tableName){
		return linkClient.toInternalUrl(new NodewatchThresholdEditLink(clientName, tableName));
	}

	public String thresholdDelete(String clientName, String tableName){
		return linkClient.toInternalUrl(new NodewatchThresholdDeleteLink(clientName, tableName));
	}

}
