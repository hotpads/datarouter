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
package io.datarouter.gcp.gcs.client;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.td;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.utils.URIBuilder;

import io.datarouter.gcp.gcs.DatarouterGcsClient;
import io.datarouter.gcp.gcs.GcsClientType;
import io.datarouter.gcp.gcs.config.DatarouterGcpGcsPaths;
import io.datarouter.gcp.gcs.node.GcsNode;
import io.datarouter.gcp.gcs.web.GcsBucketHandler;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.NodeTool;
import io.datarouter.web.browse.DatarouterClientWebInspector;
import io.datarouter.web.browse.dto.DatarouterWebRequestParamsFactory;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.params.Params;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;

public class GcsWebInspector implements DatarouterClientWebInspector{

	@Inject
	private DatarouterNodes nodes;
	@Inject
	private DatarouterWebRequestParamsFactory paramsFactory;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private ClientOptions clientOptions;
	@Inject
	private GcsClientManager gcsClientManager;
	@Inject
	private DatarouterGcpGcsPaths paths;

	@Override
	public Mav inspectClient(Params params, HttpServletRequest request){
		String contextPath = request.getContextPath();
		var clientParams = paramsFactory.new DatarouterWebRequestParams<>(params, GcsClientType.class);
		var clientId = clientParams.getClientId();
		if(clientId == null){
			return new MessageMav("Client not found");
		}

		var clientName = clientId.getName();
		Map<String,String> allClientOptions = clientOptions.getAllClientOptions(clientName);
		var content = div(
				buildClientPageHeader(clientName),
				buildClientOptionsTable(allClientOptions),
				buildNodeTable(contextPath, clientId),
				buildBucketTable(contextPath, clientId))
				.withClass("container my-3");

		return pageFactory.startBuilder(request)
				.withTitle("Datarouter Client - Gcs")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(content)
				.buildMav();
	}

	private DivTag buildNodeTable(String contextPath, ClientId clientId){
		List<GcsNodeDto> nodeDtos = Scanner.of(nodes.getTableNamesForClient(clientId.getName()))
				.map(tableName -> nodes.getPhysicalNodeForClientAndTable(
						clientId.getName(),
						tableName))
				.map(NodeTool::extractSinglePhysicalNode)
				.map(GcsNode.class::cast)
				.map(gcsNode -> new GcsNodeDto(gcsNode.getBucket(), gcsNode.getRootPath().toString()))
				.list();
		var table = new J2HtmlTable<GcsNodeDto>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withHtmlColumn("Browse", row -> {
					String href = new URIBuilder()
							.setPath(contextPath + paths.datarouter.clients.gcpGcs.listObjects.toSlashedString())
							.addParameter(GcsBucketHandler.P_client, clientId.getName())
							.addParameter(GcsBucketHandler.P_bucket, row.bucket)
							.addParameter(GcsBucketHandler.P_prefix, row.rootPath)
							.toString();
					return td(a("Browse").withHref(href));
				})
				.withColumn("Bucket", row -> row.bucket)
				.withColumn("Root Path", row -> row.rootPath)
				.build(nodeDtos);
		return div(h4("Nodes - " + nodeDtos.size()), table)
				.withClass("container-fluid my-4")
				.withStyle("padding-left: 0px");
	}

	private record GcsNodeDto(
			String bucket,
			String rootPath){
	}

	private DivTag buildBucketTable(String contextPath, ClientId clientId){
		DatarouterGcsClient client = gcsClientManager.getClient(clientId);
		List<GcsBucketDto> buckets = client.scanBuckets()
				.map(bucket -> new GcsBucketDto(
						clientId.getName(),
						bucket.getName(),
						bucket.getLocationType(),
						bucket.getLocation(),
						bucket.getCreateTime()))
				.sort(Comparator.comparing(bucket -> bucket.bucketName.toLowerCase()))
				.list();
		var table = new J2HtmlTable<GcsBucketDto>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withHtmlColumn("Name", bucket -> {
					String href = new URIBuilder()
							.setPath(contextPath + paths.datarouter.clients.gcpGcs.listObjects.toSlashedString())
							.addParameter(GcsBucketHandler.P_client, bucket.clientName)
							.addParameter(GcsBucketHandler.P_bucket, bucket.bucketName)
							.addParameter(GcsBucketHandler.P_delimiter, "/")
							.toString();
					return td(a(bucket.bucketName).withHref(href));
				})
				.withColumn("Location Type", bucket -> bucket.locationType)
				.withColumn("Location", bucket -> bucket.location)
				.withColumn("Created", bucket -> Instant.ofEpochMilli(bucket.creationDate), Instant::toString)
				.build(buckets);
		return div(h4("Buckets - " + buckets.size()), table)
				.withClass("container-fluid my-4")
				.withStyle("padding-left: 0px");
	}

	private record GcsBucketDto(
			String clientName,
			String bucketName,
			String locationType,
			String location,
			Long creationDate){
	}

}
